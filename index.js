const express = require('express')
 var bodyParser = require('body-parser')
const app = express();
/*app.use(bodyParser.raw({
  type: 'image/png',
  limit: '10mb'
}));*/
/*
app.use(bodyParser.raw({ inflate: true, limit: '1000kb', type: '' }));
*/
const fs = require('fs');
var image;
app.get('/', (req, res) => {
	console.log("get");
  return res.send('<img src="download">');
});
 
app.post('/upload', (req, res) => {
image = req.body;
console.log(image);
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
app.get('/download', (req, res) => {
res.set('Content-Type', 'image/png')
  return res.send(image);
});
 /*
const upload = multer({
  dest: "./"
  // you might also want to set some limits: https://github.com/expressjs/multer#limits
});
*/
var storage = multer.memoryStorage()
var upload = multer({ storage: storage })

app.post(
  "/upload1",
  upload.single("file" /* name attribute of <file> element in your form */),
  (req, res) => {
	  console.log("saving:" + Object.keys(req.file));
	  image = req.file.buffer;


  }
);