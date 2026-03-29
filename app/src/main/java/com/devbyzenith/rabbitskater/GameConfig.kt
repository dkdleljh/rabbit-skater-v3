package com.devbyzenith.rabbitskater

object GameConfig {
    object Player {
        const val INITIAL_X = 100f
        const val GRAVITY = 0.8f
        const val JUMP_FORCE = -20f
        const val DOUBLE_JUMP_FORCE = -18f
        const val WIDTH = 70f
        const val HEIGHT = 75f
    }

    object Game {
        const val INITIAL_SPEED = 5f
        const val MAX_SPEED = 15f
        const val SPEED_INCREMENT = 0.01f
        const val SPEED_INCREASE_INTERVAL = 100
        const val OBSTACLE_SPAWN_INTERVAL = 60
        const val SCORE_PER_OBSTACLE = 10
    }

    object Rendering {
        const val FRAME_RATE = 60
        const val FRAME_DELAY_MS = 16
        const val GROUND_HEIGHT = 100f
        const val PLAYER_Y_OFFSET = 150f
    }

    object Sprites {
        const val CHARACTER_SCALE = 70f
        const val ANIMATION_SPEED = 10
    }

    object UI {
        const val SCORE_TEXT_SIZE = 48f
        const val TITLE_TEXT_SIZE = 56f
        const val GAME_OVER_TEXT_SIZE = 72f
        const val SCORE_DISPLAY_TEXT_SIZE = 40f
        const val TAP_TEXT_SIZE = 36f
    }

    object Colors {
        const val SKY_TOP = "#1A1A2E"
        const val SKY_MIDDLE = "#16213E"
        const val SKY_BOTTOM = "#0F3460"
        const val GROUND = "#2E7D32"
        const val GROUND_LINE = "#4CAF50"
        const val OBSTACLE = "#E53935"
        const val TEXT_SHADOW_COLOR = "#000000"
    }
}
