// Released into the public domain, 15 August 2010

// This program demonstrates how a C application can access some of the Android
// API via the SL4A (Scripting Languages for Android, formerly "ASE", or Android
// Scripting Environment) RPC mechanism.  It works either from a host computer
// or as a native binary compiled with the NDK (rooted phone required, I think)

// SL4A is a neat Android app that provides support for many popular scripting
// languages like Python, Perl, Ruby and TCL.  SL4A exposes a useful subset of
// the Android API in a clever way: by setting up a JSON RPC server.  That way,
// each language only needs to implement a thin RPC client layer to access the
// whole SL4A API.

// The Android NDK is a C compiler only intended for writing optimized
// subroutines of "normal" Android apps written in Java.  So it doesn't come
// with any way to access the Android API.

// This program uses the excellent "Jansson" JSON library to talk to SL4A's
// RPC server, effectively adding native C programs to the list of languages
// supported by SL4A.

// To try it, first install SL4A: http://code.google.com/p/android-scripting/
// 
// Start a private server with View->Interpreters->Start Server
//
// Note the port number the server is running on by pulling down the status
// bar and tapping "SL4A service". 

// This program works just fine as either a native Android binary or from a
// host machine.

// ------------

// To compile on an ordinary linux machine, first install libjansson.  Then:

// $ gcc -ljansson ndk-to-sl4a.c -o ndk-to-sl4a

// To access SL4A on the phone use "adb forward tcp:XXXXX tcp:XXXXX" to port
// forward the SL4A server port from your host to the phone.  See this
// page for more details:
// http://code.google.com/p/android-scripting/wiki/RemoteControl

// ------------

// To compile using the NDK:
//   1. Make sure you can compile "Hello, world" using the NDK.  See:
//      http://credentiality2.blogspot.com/2010/08/native-android-c-program-using-ndk.html
//
//   2. If you followed the above instructions, you have a copy of the agcc.pl
//      wrapper that calls the NDK's gcc compiler with the right options for
//      standalone apps.
//
//   3. Unpack a fresh copy of the jansson sources.  Tell configure to build for
//      Android:
//
// $ CC=agcc.pl ./configure --host=arm
// $ make
//
//   4. Cross your fingers and go!  (I'm quite certain there's a more elegant
//      way to do this)
//
// $ agcc.pl -I/path/to/jansson-1.3/src -o ndk-to-sl4a-arm ndk-to-sl4a.c /path/to/jansson-1.3/src/*.o
//
//   5. Copy to the phone and run it with the port of the SL4A server!
 
#include <stdio.h>
#include <jansson.h>
#include <unistd.h>
#include <string.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 

// This mimics SL4A's android.py, constructing a JSON RPC object and 
// sending it to the SL4A server.
int sl4a_rpc(int socket_fd, char *method, json_t *params) {
  static int request_id = 0; // monotonically increasing counter

  json_t *root = json_object();

  json_object_set(root, "id", json_integer(request_id));
  request_id++;

  json_object_set(root, "method", json_string(method));

  if (params == NULL) {
    params = json_array();
    json_array_append(params, json_null());
  }

  json_object_set(root, "params", params);

  char *command = json_dumps(root, JSON_PRESERVE_ORDER | JSON_ENSURE_ASCII);
  printf("command string:'%s'\n", command);

  write(socket_fd, command, strlen(command));
  write(socket_fd, "\n", strlen("\n"));
  
  // At this point we just print the response, but really we should buffer it
  // up into a single string, then pass it to json_loads() for decoding.
  printf("Got back:\n");
  while (1) {
    char c;
    read(socket_fd, &c, 1);
    printf("%c", c);
    if (c == '\n') {
      break;
    }
  }
  fflush(stdout);
  return 0;
}


// This function is just boilerplate TCP socket setup code
int init_socket(char *hostname, int port) {
  int socket_fd = socket(AF_INET, SOCK_STREAM, 0);
  if (socket_fd == -1) {
    perror("Error creating socket");
    return 0;
  }

  struct hostent *host = gethostbyname(hostname);
  if (host == NULL) {
    perror("No such host");
    return -1;
  }

  struct sockaddr_in socket_address;

  int i;
  for (i=0; i < sizeof(socket_address); i++) {
    ((char *) &socket_address)[i] = 0;
  }

  socket_address.sin_family = AF_INET;

  for (i=0; i < host->h_length; i++) {
    ((char *) &socket_address.sin_addr.s_addr)[i] = ((char *) host->h_addr)[i];
  }

  socket_address.sin_port = htons(port);

  if (connect(socket_fd, (struct sockaddr *) &socket_address, sizeof(socket_address)) < 0) {
    perror("connect() failed");
    return -1;
  }

  return socket_fd; 
}

main(int argc, char **argv) {
  int port = 0;
  if (argc != 2) {
    printf("Usage: %s port\n", argv[0]);
    return 1;
  }
  port = atoi(argv[1]);

  int socket_fd = init_socket("localhost", port);
  if (socket_fd < 0) return 2;
 
  json_t *params = json_array();
  json_array_append(params, json_string("w00t!"));
  sl4a_rpc(socket_fd, "makeToast", params);
}
