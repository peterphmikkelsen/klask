function testButton() {
    const text = document.getElementById("test")

    if (text.style.color === "aqua" || text.style.color === "") {
        text.style.color = "red"
    } else {
        text.style.color = "aqua"
    }
}