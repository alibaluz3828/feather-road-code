package com.AsDfGhJ.jMiKoLp.domain.usecase

class ScoreRunUseCase {

    operator fun invoke(cleared: Boolean, failedLaunches: Int, actionsLeft: Int): Int {
        if (!cleared) {
            return 0
        }
        if (failedLaunches == 0 && actionsLeft >= 2) {
            return 3
        }
        if (failedLaunches <= 1) {
            return 2
        }
        return 1
    }
}
