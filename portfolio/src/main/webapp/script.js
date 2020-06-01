function formatComment(comment) {
    out = "<div class=\"comment card\"><p>";
    out += comment;
    out += "</p></div>";
    return out;
}

function getComments() {
  let area = document.getElementById('comments-area');
  fetch('/data').then(response => response.json())
  .then((json) => {
    json.forEach(comment => {
      area.innerHTML += formatComment(comment);
    })
  });
}