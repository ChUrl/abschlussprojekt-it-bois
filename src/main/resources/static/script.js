// Add the following code if you want the name of the file appear on select
$(".custom-file-input").on("change", function () {
    const fileName = $(this).val().split("\\").pop();
    $(this).siblings(".custom-file-label").addClass("selected").html(fileName);
});

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
