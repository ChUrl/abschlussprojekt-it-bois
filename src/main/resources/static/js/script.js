// Add the following code if you want the name of the file appear on select

function disable(id) {
    $(id).prop('disabled', true);
}

function enable(id) {
    $(id).prop('disabled', false);
}

function copyLink() {
    const copyText = document.getElementById("groupLink");

    copyText.select();
    copyText.setSelectionRange(0, 99999);

    document.execCommand("copy");
}
