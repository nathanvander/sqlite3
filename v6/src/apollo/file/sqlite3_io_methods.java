package apollo.file;
import apollo.lock.sqlite3_lock_methods;
import apollo.shmem.sqlite3_shmem_methods;

public interface sqlite3_io_methods extends sqlite3_file_methods,
										sqlite3_lock_methods,
										sqlite3_shmem_methods {}