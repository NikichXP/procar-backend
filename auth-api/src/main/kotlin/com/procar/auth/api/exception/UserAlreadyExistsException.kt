package com.procar.auth.api.exception

class UserAlreadyExistsException(login: String) : RuntimeException("User with login '$login' already exists")
