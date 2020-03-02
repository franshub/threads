#include <pthread.h>
#include <stdio.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>

static pthread_mutex_t mutex;
static pthread_cond_t cond;

static int buffer = 0;

static void put() {
    pthread_mutex_lock(&mutex);
    buffer++;
    pthread_cond_signal(&cond);
    pthread_mutex_unlock(&mutex);
}

static void get() {
    pthread_mutex_lock(&mutex);
    while (buffer < 1) {
        pthread_cond_wait(&cond, &mutex);
    }
    buffer--;
    pthread_mutex_unlock(&mutex);
}

void *runner(void *arg) {
    put();
}

static void run_in_threads(int number_of_runs) {
    struct timeval tv_start;
    struct timeval tv_end;

    gettimeofday(&tv_start, NULL);
    for (int i = 0; i < number_of_runs; i++) {
        pthread_t thread;
        pthread_create(&thread, 0, runner, NULL);
        pthread_detach(thread);
    }
    for (int i = 0; i < number_of_runs; i++) {
        get();
    }
    gettimeofday(&tv_end, NULL);

    double duration = (tv_end.tv_sec - tv_start.tv_sec) * 1000.0 * 1000.0 + (tv_end.tv_usec - tv_start.tv_usec);
    printf("Duration per task in separate threads is %9.4f microseconds.\n", duration / number_of_runs);
}

static void run_in_self(int number_of_runs) {
    struct timeval tv_start;
    struct timeval tv_end;

    gettimeofday(&tv_start, NULL);
    for (int i = 0; i < number_of_runs; i++) {
        put();
    }
    for (int i = 0; i < number_of_runs; i++) {
        get();
    }
    gettimeofday(&tv_end, NULL);

    double duration = (tv_end.tv_sec - tv_start.tv_sec) * 1000.0 * 1000.0 + (tv_end.tv_usec - tv_start.tv_usec);
    printf("Duration per task in same thread is      %9.4f microseconds.\n", duration / number_of_runs);
}

static void initialise() {
    pthread_mutex_init(&mutex, 0);
    pthread_cond_init(&cond, 0);
}

static void cleanup() {
    pthread_mutex_destroy(&mutex);
    pthread_cond_destroy(&cond);
}

int main(int argc, char *argv[]) {
    initialise();

    int number_of_runs = 20000;
    for (int i = 0; i < 3; i++) {
        printf("Starting test run %d...\n", i + 1);
        run_in_self(number_of_runs);
        run_in_threads(number_of_runs);
    }

    cleanup();
    return 0;
}
