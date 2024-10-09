package org.johnson

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.apache.commons.lang3.concurrent.TimedSemaphore

fun main() {
    basicStartAThread()
    virtualThreadExecutor()
    fixedThreadPoolExecutor()
    virtualThreadExecutorWithSemaphore()
}

fun basicStartAThread() {
    val virtualThread = Thread.startVirtualThread {
        println(
            "Running task in a virtual thread: single"
        )
    }
    // Waiting for virtual threads to complete
    try {
        virtualThread.join()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

fun virtualThreadExecutor(){
    val executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    val httpExecutor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    val client = HttpClient.newBuilder()
        .executor(httpExecutor)
        .build()
    for (i in 0..99) {
        val handle = executor.submit {
            println(
                "Running task in a virtual thread: $i"
            )
            try {
                val request = HttpRequest.newBuilder()
                    .uri(URI("https://dummyjson.com/products/$i"))
                    .GET()
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                println("Response for request $i: ${response.body()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
    executor.shutdown()
}

fun fixedThreadPoolExecutor(){
    val executor: ExecutorService = Executors.newFixedThreadPool(2)
    val client = HttpClient.newHttpClient()
    for (i in 0..99) {
        executor.submit {
            println(
                "Running task in a virtual thread: $i"
            )
            try {
                val request = HttpRequest.newBuilder()
                    .uri(URI("https://dummyjson.com/products/$i"))
                    .GET()
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                println("Response for request $i: ${response.body()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
    executor.shutdown()
}

fun virtualThreadExecutorWithSemaphore(){
    val executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    val httpExecutor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    val client = HttpClient.newBuilder()
        .executor(httpExecutor)
        .build()
    val semaphore = TimedSemaphore(1, java.util.concurrent.TimeUnit.SECONDS, 2)
    for (i in 0..99) {
        executor.submit {
            semaphore.acquire()
            println(
                "Running task in a virtual thread: $i"
            )
            try {

                val request = HttpRequest.newBuilder()
                    .uri(URI("https://dummyjson.com/products/$i"))
                    .GET()
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                println("Response for request $i: ${response.body()}")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
    executor.shutdown()
}