/**
 * Created by Drakpa on 5/16/2023.
 */
function getProofRequest() {
    var data = JSON.stringify({
        proofName: "verifyUser",
        proofAttributes: [
            {
                name: "Full Name",
                restrictions: [{schema_id: "2acnD7masG23MBd7nn4xna:2:Foundational ID:1.0.1"}]
            },
            {
                name: "ID Number",
                restrictions: [{schema_id: "2acnD7masG23MBd7nn4xna:2:Foundational ID:1.0.1"}]
            },
            {
                name: "Mobile Number",
                restrictions: [],
                self_attest_allowed: true
            },
        ]
    });

    $.ajax({
        type: "POST",
        url: 'http://119.2.117.161:3004/proof-request',
        headers: {"Content-Type": "application/json"},
        data: data,
        cache: false,
        success: function (response) {
            console.log(JSON.stringify(response.data));
            alert(response.data.proofRequestThreadId + "============== thread id =============");
            natsListener(response.data.proofRequestThreadId);
            resolve(response);

        }
    });
}

function natsListener(threadId) {
    debugger;
    console.log(threadId);
    const conn = connect.connect({
        servers: ["ws://119.2.117.161:443"]
    });
    var sc = StringCodec();

    var s1 = conn.subscribe(threadId);

    var subj = s1.getSubject();
    const c = 13 - subj.length;
    const pad = "".padEnd(c);
    /* for  (var m of s1) {
     console.log(
     `[${subj}]${pad} #${s1.getProcessed()} - ${m.subject} ${
     m.data ? " " + sc.decode(m.data) : ""
     }`
     );
     const data = JSON.parse(sc.decode(m.data))
     if(data?.data?.verification_result==='ProofValidated'){
     conn.close();
     navigate("/dashboard");
     }
     }*/

}

/* function natsListener(threadId) {
 console.log(threadId);
 debugger;
 connect({ servers: ["ws://119.2.117.161:443"] }).then(function(conn) {
 const sc = StringCodec();

 const s1 = conn.subscribe(threadId);

 let subj = s1.getSubject();
 console.log("listening for: " + subj);
 const c = 13 - subj.length;
 const pad = "".padEnd(c);

 s1.forEachAsync(function(m) {
 console.log(
 "[" + subj + "]" + pad + " #" + s1.getProcessed() + " - " + m.subject + (m.data ? " " + sc.decode(m.data) : "")
 );

 const data = JSON.parse(sc.decode(m.data));
 if (data?.data?.verification_result === "ProofValidated") {
 conn.close();
 navigate("/dashboard");
 }
 });
 });
 }*/
