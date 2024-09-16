package com.example.foodies.model

data class User(val id: String?, val name: String, val email: String){

    fun toMap(): MutableMap<String, String?> {
        return mutableMapOf(
            "id" to this.id,
            "name" to this.name,
            "email" to this.email
        )

    }




}
