package io.github.cyprienj.sporz.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Roles of docs/rules.md §2.2. Serial names are the codes of the rules.
 * [minCount] and [maxCount] are the allowed numbers in
 * a composition; the astronauts fill the remaining seats (R-SET-3).
 */
@Serializable
public enum class Role(public val minCount: Int, public val maxCount: Int) {
    @SerialName("baseMutant") BaseMutant(1, 1),
    @SerialName("doctor") Doctor(1, 3),
    @SerialName("psychologist") Psychologist(0, 1),
    @SerialName("geneticist") Geneticist(0, 1),
    @SerialName("computerScientist") ComputerScientist(0, 1),
    @SerialName("hacker") Hacker(0, 1),
    @SerialName("spy") Spy(0, 1),
    @SerialName("informant") Informant(0, 1),
    @SerialName("investigator") Investigator(0, 1),
    @SerialName("traitor") Traitor(0, 1),
    @SerialName("astronaut") Astronaut(0, Int.MAX_VALUE),
    ;

    /** Roles the MJ counts in a composition; astronauts are computed. */
    public val isChosen: Boolean get() = this != Astronaut

    public companion object {
        /** Default number of doctors (§2.2). */
        public const val DEFAULT_DOCTORS: Int = 2
    }
}
