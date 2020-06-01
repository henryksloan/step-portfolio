function formatComment(comment) {
    out = "<div class=\"card\"><p>";
    out += comment;
    out += "</p></div>";
    return out;
}

function getComments() {
  fetch('/data').then(response => response.json())
  .then((json) => {
    json.forEach(comment => {
      console.log(comment);
      document.getElementById('comments').innerHTML += formatComment(comment);
    })
  });
}