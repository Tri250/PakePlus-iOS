const express = require('express');
const path = require('path');

const app = express();
const HOST = '0.0.0.0';
const PORT = process.env.PORT || 8080;

app.use(express.static(path.join(__dirname)));

app.listen(PORT, HOST, () => {
  console.log(`PakePlus Android preview server running at http://${HOST}:${PORT}`);
});
