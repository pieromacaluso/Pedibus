function showAlert(message) {
    let div = document.createElement("div");
    div.setAttribute('id', 'err-mess');
    div.setAttribute('class', 'alert alert-danger');
    div.setAttribute('role','alert');
    div.innerHTML = message;
    document.getElementById('form-reg').appendChild(div);
    console.log(message);
}

function showSuccess(message) {
    let div = document.createElement("div");
    div.setAttribute('id', 'success-mess');
    div.setAttribute('class', 'alert alert-success');
    div.setAttribute('role', 'success');
    div.innerHTML = message;
    document.getElementById('form-reg').appendChild(div);
    console.log(message);
}

function postForm() {

    let endpoint = window.location.href;
    console.log("start posting...");
    console.log(window.location.pathname);
    // rimuovo errori precedenti se esistenti
    let errors = document.getElementById('err-mess');
    if (errors !== null) {
        errors.remove();
    }
    let success = document.getElementById('success-mess');
    if (success !== null) {
        success.remove();
    }
    let passRgx = /^[\w\d]{4,25}$/;
    let pass = document.getElementById('pass').value;
    let pass1 = document.getElementById('passMatch').value;
    if (passRgx.test(pass)) {
        if (pass === pass1) {
            // invio richiesta
            fetch(endpoint, {
                method: 'post',
                headers: {
                    'Accept': 'application/json, text/plain, */*',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({password: pass, passMatch: pass1})
            }).then(function (response) {
                console.log(response.status); // returns 200 or 404
                if (response.status === 200) {
                    showSuccess('Password changed correctly!');
                } else {
                    showAlert('Problem in the recovering of password. Maybe it is expired.');
                }
            }).then(res => console.log(res));
        } else {
            showAlert('Le password non coincidono');
        }
    } else {
        showAlert('Password non valida');
    }
}

