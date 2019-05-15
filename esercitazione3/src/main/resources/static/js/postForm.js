function showAlert(message) {
    let div = document.createElement("div");
    div.setAttribute('id', 'err-mess');
    div.setAttribute('class', 'alert alert-danger');
    div.setAttribute('role','alert');
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
    // regex da RFC su email
    let emailRgx = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    let passRgx = /^[\w\d]{4,25}$/;
    let email = document.getElementById('email').value;
    let pass = document.getElementById('pass').value;
    let pass1 = document.getElementById('pass1').value;
    if (emailRgx.test(email)) {
        if (passRgx.test(pass)) {
            if (pass === pass1) {
                // invio richiesta
                fetch(endpoint, {
                    method: 'post',
                    headers: {
                        'Accept': 'application/json, text/plain, */*',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({email: email, password: pass, passMatch: pass1})
                }).then(res => res.json())
                    .then(res => console.log(res));
            } else {
                showAlert('Le password non coincidono');
            }
        } else {
            showAlert('Password non valida');
        }
    } else {
        showAlert('Email non valida');
    }
}

