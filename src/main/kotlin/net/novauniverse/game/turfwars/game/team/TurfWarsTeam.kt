package net.novauniverse.game.turfwars.game.team

enum class TurfWarsTeam {
    TEAM_1, TEAM_2;

    fun getOpposite(): TurfWarsTeam {
        if(this == TEAM_1) {
            return TEAM_2
        }
        return TEAM_1
    }
}