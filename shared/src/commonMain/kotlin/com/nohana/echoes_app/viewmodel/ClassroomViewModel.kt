package com.nohana.echoes_app.classroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nohana.echoes_app.network.dto.Classroom
import com.nohana.echoes_app.network.dto.ClassroomInvite
import com.nohana.echoes_app.network.dto.ClassroomMaterial
import com.nohana.echoes_app.network.dto.MaterialUpload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException

data class ClassroomState(
    val classrooms: List<Classroom> = emptyList(),
    val search: String = "",
    val page: Int = 0,
    val pageCount: Int = 0,
    val detail: Classroom? = null,
    val materials: List<ClassroomMaterial> = emptyList(),
    val invite: ClassroomInvite? = null,
    val busy: Boolean = true,
    val error: String? = null,
    val message: String? = null,
)

class ClassroomViewModel(private val api: ClassroomApi) : ViewModel() {
    private val mutableState = MutableStateFlow(ClassroomState(busy = false))
    val state = mutableState.asStateFlow()

    init {
        list()
    }

    fun list(search: String = state.value.search, page: Int = 0) = run {
        refreshList(search.trim(), page)
    }

    fun open(id: String) = run { refreshDetail(id) }

    fun back() {
        if (state.value.busy) return
        mutableState.update { it.copy(detail = null, materials = emptyList(), invite = null) }
        list()
    }

    fun join(rawCode: String) = run {
        val code = rawCode.trim().uppercase()
        require(Regex("[A-HJ-NP-Z2-9]{6}").matches(code)) { "Informe um convite de 6 caracteres." }
        val classroom = api.join(code)
        mutableState.update {
            it.copy(detail = classroom, materials = emptyList(), message = "Inscrição realizada.")
        }
        refreshDetail(classroom.id)
    }

    fun generateInvite() = runForDetail { classroom ->
        val invite = api.invite(classroom.id)
        mutableState.update {
            it.copy(detail = classroom.copy(joinCode = invite.code), invite = invite)
        }
        viewModelScope.launch {
            delay(invite.expiresInSeconds.coerceAtLeast(0) * 1000L)
            mutableState.update { current ->
                val detail = current.detail
                if (detail?.id == classroom.id && detail.joinCode == invite.code) {
                    current.copy(detail = detail.copy(joinCode = null), invite = null)
                } else current
            }
        }
    }

    fun addMaterial(title: String, description: String, file: suspend () -> MaterialUpload) =
        changeMaterial("Material adicionado.") { id ->
            validateText(title, description)
            api.addMaterial(id, title.trim(), description.trim(), file())
        }

    fun updateMaterial(materialId: String, title: String, description: String) =
        changeMaterial("Material atualizado.") { id ->
            validateText(title, description)
            api.updateMaterial(id, materialId, title.trim(), description.trim())
        }

    fun replaceFile(materialId: String, file: suspend () -> MaterialUpload) =
        changeMaterial("Arquivo substituído.") { id ->
            api.replaceFile(id, materialId, file())
        }

    fun deleteMaterial(materialId: String) = changeMaterial("Material removido.") { id ->
        api.deleteMaterial(id, materialId)
    }

    fun saveMaterial(
        classroomId: String,
        materialId: String,
        save: suspend (ClassroomApi, String, String) -> Unit,
    ) = run {
        save(api, classroomId, materialId)
        mutableState.update { it.copy(message = "Arquivo salvo.") }
    }

    fun showError(message: String) {
        mutableState.update { it.copy(error = message, message = null) }
    }

    private fun validateText(title: String, description: String) {
        require(title.trim().length in 1..150) { "Informe um título de até 150 caracteres." }
        require(description.length <= 1000) { "A descrição deve ter até 1.000 caracteres." }
    }

    private fun changeMaterial(message: String, change: suspend (String) -> Unit) =
        runForDetail { classroom ->
            change(classroom.id)
            mutableState.update { it.copy(message = message) }
            val materials = try {
                api.materials(classroom.id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showError("A alteração foi salva. Atualize a lista para ver o resultado.")
                return@runForDetail
            }
            mutableState.update { it.copy(materials = materials) }
        }

    private fun runForDetail(action: suspend (Classroom) -> Unit) {
        val classroom = state.value.detail ?: return
        run { action(classroom) }
    }

    private suspend fun refreshList(search: String, page: Int) {
        val result = api.list(search, page)
        if (page > 0 && result.content.isEmpty() && page >= result.pageCount) {
            refreshList(search, (result.pageCount - 1).coerceAtLeast(0))
            return
        }
        mutableState.update {
            it.copy(classrooms = result.content, search = search, page = page, pageCount = result.pageCount)
        }
    }

    private suspend fun refreshDetail(id: String) {
        val classroom = api.detail(id)
        val materials = api.materials(id)
        mutableState.update {
            it.copy(detail = classroom, materials = materials, invite = null)
        }
    }

    private fun run(action: suspend () -> Unit) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ClassroomApiException) {
                showError(e.message ?: "Não foi possível concluir a operação.")
            } catch (e: IllegalArgumentException) {
                // Validação local usa mensagens fixas. Erros de decodificação ficam genéricos.
                val message = e.message
                showError(if (e is kotlinx.serialization.SerializationException) {
                    "O servidor retornou dados em um formato inesperado."
                } else message ?: "Dados inválidos.")
            } catch (_: IOException) {
                showError("Não foi possível comunicar com o servidor ou acessar o arquivo.")
            } catch (_: Exception) {
                showError("Não foi possível concluir a operação. Tente novamente.")
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }

    override fun onCleared() {
        api.close()
        super.onCleared()
    }
}
