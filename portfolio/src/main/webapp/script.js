function formatComment(comment) {
    out = "<div class=\"comment card\"><p>";
    out += comment;
    out += "</p></div>";
    return out;
}

function getComments() {
  let comments = document.getElementById('comments-container');
  comments.innerHTML = "";
  let num_comments = parseInt(document.getElementById('num-comments').value);
  fetch('/data?num-comments=' + num_comments).then(response => response.json())
  .then((json) => {
    json.forEach(comment => {
      comments.innerHTML += formatComment(comment);
    })
  });
}

$(document).ready(function() {
    $('#submit-comment').prop('disabled',true);
    $('#new-comment').keyup(function() {
        $('#submit-comment').prop('disabled', (this.value == "")); 
    })
})