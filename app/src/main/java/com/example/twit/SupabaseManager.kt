package com.example.twit

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://qytlpcobtbnpgpvkgxca.supabase.co",   // 🔹 replace with your project URL
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF5dGxwY29idGJucGdwdmtneGNhIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0OTY0NDk4MiwiZXhwIjoyMDY1MjIwOTgyfQ.r0PHLN1gDPlJz4DXUZtVnQNgbBkDLx7uLK7o7F6Zu8U"                         // 🔹 replace with your anon key
        ) {
            install(Postgrest)
        }
    }
}
