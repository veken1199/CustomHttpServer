# Http Server



 implemented a simple HTTP server that handle requests sent to the locak host (web servers).

  - Get Request
  - Post Request
  - Content Type - plain text/json
  - Content Disposition
  - Read and Write to files
  - Multithreaded 
  - Concurrent File Read/Write management 

# Using the application:

  - java -jar Httpf.jar -p portnumber<int> -d directory<string> -v<boolean>

  ## using my httpClient
  - java -jar Httpc.jar -help post
  - java -jar Httpc.jar -help get
  - java -jar Httpc.jar -get "localhost:50/" -h"Accept:application/json" -v
  - java -jar Httpc.jar -post "localhost:50/newFile.txt" -d"content:any content,overwrite:true/false" 


