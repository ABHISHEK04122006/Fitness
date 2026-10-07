package com.example.fitness.feature.admin

enum class AdminModule(
    val title: String,
    val description: String,
    val category: String,
    val targetStep: String
) {
    DASHBOARD(
        title = "Dashboard",
        description = "System health, administrative overview, and real-time registered user metrics.",
        category = "Overview",
        targetStep = "Active (Step 2)"
    ),
    USERS(
        title = "Users",
        description = "User directory, custom claims management, profile moderation, and role assignment.",
        category = "Management",
        targetStep = "Coming in Step 3"
    ),
    NOTIFICATIONS(
        title = "Notifications",
        description = "Global push notification broadcasts, workout reminder scheduling, and user targeting.",
        category = "Engagement",
        targetStep = "Coming in Step 4"
    ),
    WORKOUTS(
        title = "Workouts",
        description = "Global workout database, routine builder, muscle targets, and duration management.",
        category = "Content",
        targetStep = "Coming in Step 5"
    ),
    EXERCISES(
        title = "Exercises",
        description = "Exercise library, instructional video assets, equipment lists, and form cues.",
        category = "Content",
        targetStep = "Coming in Step 6"
    ),
    PROGRAMS(
        title = "Programs",
        description = "Multi-week structured curricula (30-Day, Hypertrophy, Strength, Athlete Performance).",
        category = "Curriculum",
        targetStep = "Coming in Step 7"
    ),
    CONTENT(
        title = "Content",
        description = "Nutrition guides, wellness articles, community announcements, and FAQs.",
        category = "Curriculum",
        targetStep = "Coming in Step 8"
    ),
    ANALYTICS(
        title = "Analytics",
        description = "User retention curves, daily workout completions, and engagement metrics.",
        category = "Insights",
        targetStep = "Coming in Step 9"
    ),
    SETTINGS(
        title = "Settings",
        description = "Security rules configuration, Firebase service account tools, and audit logs.",
        category = "System",
        targetStep = "Coming in Step 10"
    )
}
