package io.github.cyprienj.sporz.ui.common

import androidx.annotation.StringRes
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.ui.theme.Tone

/** French name of the role (design.html). */
@get:StringRes
val Role.nameRes: Int
    get() = when (this) {
        Role.BaseMutant -> R.string.role_base_mutant
        Role.Doctor -> R.string.role_doctor
        Role.Psychologist -> R.string.role_psychologist
        Role.Geneticist -> R.string.role_geneticist
        Role.ComputerScientist -> R.string.role_computer_scientist
        Role.Hacker -> R.string.role_hacker
        Role.Spy -> R.string.role_spy
        Role.Traitor -> R.string.role_traitor
        Role.Astronaut -> R.string.role_astronaut
    }

/** One-line description of the role (composition screen). */
@get:StringRes
val Role.hintRes: Int
    get() = when (this) {
        Role.BaseMutant -> R.string.role_base_mutant_hint
        Role.Doctor -> R.string.role_doctor_hint
        Role.Psychologist -> R.string.role_psychologist_hint
        Role.Geneticist -> R.string.role_geneticist_hint
        Role.ComputerScientist -> R.string.role_computer_scientist_hint
        Role.Hacker -> R.string.role_hacker_hint
        Role.Spy -> R.string.role_spy_hint
        Role.Traitor -> R.string.role_traitor_hint
        Role.Astronaut -> R.string.role_astronaut_hint
    }

/** Camp color of the role at the start of the game (red for the mutant camp). */
val Role.startTone: Tone
    get() = if (this == Role.BaseMutant || this == Role.Traitor) Tone.Mutant else Tone.Healthy
