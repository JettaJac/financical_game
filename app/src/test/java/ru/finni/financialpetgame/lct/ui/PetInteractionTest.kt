package ru.finni.financialpetgame.lct.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PetInteractionTest {
    @Test
    fun overlayBackClosesOverlayEvenInsideExitInterval() {
        assertEquals(
            BackPressAction.CloseOverlay,
            backPressAction(
                previousPressAt = 1_000L,
                currentPressAt = 1_500L,
                hasDemoHint = false,
                hasActiveEvent = false,
                activeEventCanClose = false,
                hasOverlay = true,
            ),
        )
    }

    @Test
    fun secondBackExitsOnlyFromRootScreen() {
        assertEquals(
            BackPressAction.Exit,
            backPressAction(
                previousPressAt = 1_000L,
                currentPressAt = 1_500L,
                hasDemoHint = false,
                hasActiveEvent = false,
                activeEventCanClose = false,
                hasOverlay = false,
            ),
        )
    }

    @Test
    fun dragInEitherDirectionStretchesPetAndClampsAtMaximum() {
        assertEquals(1.025f, petScaleForDrag(100f, 200f), 0.0001f)
        assertEquals(1.025f, petScaleForDrag(-100f, 200f), 0.0001f)
        assertEquals(1.05f, petScaleForDrag(400f, 200f), 0.0001f)
        assertEquals(1.05f, petScaleForDrag(-400f, 200f), 0.0001f)
    }

    @Test
    fun zeroDragKeepsPetAtOriginalSize() {
        assertEquals(1f, petScaleForDrag(0f, 200f), 0.0001f)
    }
}
