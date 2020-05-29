function getGreeting() {
  fetch('/data').then(response => response.text()).then((quote) => {
    document.getElementById('greeting-container').innerText = quote;
  });
}