package com.cataloghub.android.background

class MultipleErrorsException(val errors: List<Throwable>) : Exception("Multiple errors occurred: $errors")
