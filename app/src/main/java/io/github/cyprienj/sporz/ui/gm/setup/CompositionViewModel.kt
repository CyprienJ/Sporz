package io.github.cyprienj.sporz.ui.gm.setup

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import io.github.cyprienj.sporz.engine.setup.CompositionCheck
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import io.github.cyprienj.sporz.engine.setup.check
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

/** Composition screen (ticket C4). Every change is saved in the session right away. */
@HiltViewModel
class CompositionViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    val playerCount: Int = session.draft.value?.seats?.size ?: 0

    private val _composition = MutableStateFlow(
        session.draft.value?.composition ?: SuggestedCompositions.forPlayers(playerCount),
    )
    val composition: StateFlow<Composition> = _composition.asStateFlow()

    init {
        save(_composition.value)
    }

    val check: CompositionCheck get() = _composition.value.check(playerCount)

    /**
     * "+" allowed: below the role maximum and a seat is left for it (R-SET-3).
     * A doctor also takes a seat away from the genome draw (R-SET-8).
     */
    fun canAdd(role: Role): Boolean {
        val c = _composition.value
        val genomesStillFit = role != Role.Doctor || c.extraHosts + c.resistants < genomeSeats(c)
        return role.isChosen && role != Role.BaseMutant && c.count(role) < role.maxCount &&
            c.astronauts(playerCount) > 0 && genomesStillFit
    }

    fun canRemove(role: Role): Boolean {
        val c = _composition.value
        return role.isChosen && role != Role.BaseMutant && c.count(role) > role.minCount
    }

    fun add(role: Role) {
        if (canAdd(role)) update { it.copy(roles = it.roles + (role to it.count(role) + 1)) }
    }

    fun remove(role: Role) {
        if (!canRemove(role)) return
        update {
            val roles = it.roles + (role to it.count(role) - 1)
            it.copy(roles = roles.filterValues { count -> count > 0 })
        }
    }

    /** Players who can get an extra host or resistant genome (R-SET-8). */
    private fun genomeSeats(c: Composition): Int = playerCount - c.count(Role.BaseMutant) - c.count(Role.Doctor)

    fun canAddGenome(): Boolean = _composition.value.let { it.extraHosts + it.resistants < genomeSeats(it) }

    fun addHost() { if (canAddGenome()) update { it.copy(extraHosts = it.extraHosts + 1) } }
    fun removeHost() { update { it.copy(extraHosts = (it.extraHosts - 1).coerceAtLeast(0)) } }
    fun addResistant() { if (canAddGenome()) update { it.copy(resistants = it.resistants + 1) } }
    fun removeResistant() { update { it.copy(resistants = (it.resistants - 1).coerceAtLeast(0)) } }

    fun setVariants(variants: Variants) = update { it.copy(variants = variants) }

    /** Suggérer: the table of rules §2.5, keeping the variants. */
    fun suggest() = update { SuggestedCompositions.forPlayers(playerCount, it.variants) }

    /** Deals the roles; false if the composition is not valid. */
    fun deal(random: Random = Random.Default): Boolean {
        if (!check.isValid) return false
        session.deal(random)
        return true
    }

    private fun update(change: (Composition) -> Composition) {
        val updated = change(_composition.value)
        _composition.value = updated
        save(updated)
    }

    private fun save(composition: Composition) = session.setComposition(composition)
}
