package org.octavius.modules.sandbox.domain

import org.octavius.domain.EnumWithFormatter
import org.octavius.modules.sandbox.localization.SandboxTr

enum class SandboxPriority : EnumWithFormatter<SandboxPriority> {
    Low,
    Medium,
    High,
    Critical;

    override fun toDisplayString(): String {
        return when (this) {
            Low -> SandboxTr.Priority.low()
            Medium -> SandboxTr.Priority.medium()
            High -> SandboxTr.Priority.high()
            Critical -> SandboxTr.Priority.critical()
        }
    }
}
