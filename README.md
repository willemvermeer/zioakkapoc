# zioakkapoc
Proof of concept of combining akka http and ZIO

When run the following URL returns "42":
http://localhost:8080/zio

The project is based on the lightbend akka http quickstart project.

Plan:
- replace/remove the current actor based code
- pass request parameters into ZIO calls
- experiment with composing various ZIO calls
- make response dependent on ZIO return Error or An object
- experiment with the module pattern, see: https://zio.dev/docs/howto/howto_use_module_pattern
- use ZIO test for unit tests, see: https://zio.dev/docs/usecases/usecases_testing
- what to use for an outbound http request?
- etc etc