const express = require('express')
 var bodyParser = require('body-parser')
const app = express();
const fs = require('fs');
app.get('/', (req, res) => {
	console.log("get");
  return res.send('Received a GET HTTP method');
});
 
app.post('/', (req, res) => {
console.log("post");
console.log(req);
return res.send('Received a POST HTTP method');
});
 

app.put('/', (req, res) => {
	console.log("put");
  return res.send('Received a PUT HTTP method');
});
 
app.delete('/', (req, res) => {
	console.log("delete");
  return res.send('Received a DELETE HTTP method');
});
 
app.listen(81, () =>
  console.log(`Example app listening on port 81!`),
);

const multer = require("multer");

const handleError = (err, res) => {
  res
    .status(500)
    .contentType("text/plain")
    .end("Oops! Something went wrong!");
};

const upload = multer({
  dest: "./"
  // you might also want to set some limits: https://github.com/expressjs/multer#limits
});


app.post(
  "/upload",
  upload.single("file" /* name attribute of <file> element in your form */),
  (req, res) => {
	  console.log("saving:" + req.file.originalname);
	        fs.rename(req.file.path, req.file.originalname, err => {
        if (err) return handleError(err, res);

        res
          .status(200)
          .contentType("text/plain")
          .end("File uploaded!");
      });

  }
);