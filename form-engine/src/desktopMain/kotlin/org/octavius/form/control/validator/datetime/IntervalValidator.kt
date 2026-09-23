package org.octavius.form.control.validator.datetime

import org.octavius.form.control.base.ControlContext
import org.octavius.form.control.base.ControlState
import org.octavius.form.control.base.ControlValidator
import org.octavius.form.control.base.IntervalValidation
import org.octavius.form.localization.FormTr
import kotlin.time.Duration

class IntervalValidator(
    private val validationOptions: IntervalValidation? = null
) : ControlValidator<Duration>() {

    private fun formatDuration(duration: Duration): String {
        return duration.toComponents { hours, minutes, seconds, _ ->
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    override fun validateSpecific(controlContext: ControlContext, state: ControlState<*>) {
        val value = state.value.value as? Duration ?: return
        val errors = mutableListOf<String>()

        validationOptions?.let { options ->
            options.min?.let { min ->
                if (value < min) {
                    errors.add(FormTr.Validation.minInterval(formatDuration(min)))
                }
            }
            options.max?.let { max ->
                if (value > max) {
                    errors.add(FormTr.Validation.maxInterval(formatDuration(max)))
                }
            }
        }
        errorManager.setFieldErrors(controlContext.fullStatePath, errors)
    }
}