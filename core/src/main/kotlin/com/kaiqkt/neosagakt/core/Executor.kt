package com.kaiqkt.neosagakt.core

public sealed class Executor

public data class Process(val transaction: Transaction): Executor()
public data class Retry(val transaction: Transaction, val cause: Throwable? = null): Executor()
public data class Abort(val transaction: Transaction, val cause: Throwable? = null): Executor()
public data class Finish(val transaction: Transaction): Executor()