const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

const COLLECTION_STATISTIQUES = "statistiques";
const MOTIF_INTERVENTION = "Intervention";

exports.cynoOperations = functions.firestore.document('specialties/cyno/activities/{activityId}')
  .onCreate((snap, context) => {
    // Get an object representing the document
    // e.g. {'name': 'Marie', 'age': 66}
    let now = new Date();
    let specialty = snap.data()['specialty'];
    let motifOperation = snap.data()['motif'];
    let typeOperation = snap.data()['type'];
    let agentsOnOperation = snap.data()['agentsOperation'];
    let materialsCyno = snap.data()['materialsCyno'];

    let year = now.getFullYear();

    console.log("data : " + motifOperation);

    addOperationStats(motifOperation, specialty, year, typeOperation)

    addAgentStats(agentsOnOperation, specialty, year, typeOperation);

    /**
   * Push stats Dogs
   * */
    materialsCyno.forEach(chien => {
      console.log("Doc " + chien.name);
      console.log("Doc " + chien.time);

      let countType = 0;
      let countTime = 0;

      let docChien = db.collection(COLLECTION_STATISTIQUES)
        .doc(year.toString());

      return docChien
        .get()
        .then(chienStat => {
          if (!chienStat.exists) {
            console.log("No stats actually present - count does not exist");
          } else {
            console.log("count - dog : " + JSON.stringify(chienStat.data()));
            //Check if the dog has already a stat present or not
            if (chienStat.data()[chien.name] !== undefined) {
              let types = chienStat.data()[chien.name]['type'];
              console.log("count - types : " + JSON.stringify(types));
              for (var type of Object.keys(types)) {
                if (typeOperation == type) {
                  countType = types[type];
                  break;
                }
              }
              let times = chienStat.data()[chien.name]['time'];
              console.log("count - times : " + JSON.stringify(times));
              for (var time of Object.keys(times)) {
                if (typeOperation == type) {
                  countTime = times[time];
                  break;
                }
              }
            }

          }
          let objDog = {};
          let objType = {};
          let objTime = {};
          let map = {};
          objType[typeOperation] = countType + 1;
          objTime[typeOperation] = countTime + chien.time;
          map['type'] = objType;
          map['time'] = objTime;
          objDog[chien.name] = map
          console.log("count - obj : " + JSON.stringify(objDog));
          return docChien
            .set(objDog, { merge: true });
        })
    });
  });

exports.sdOperations = functions.firestore.document('specialties/sd/activities/{activityId}')
  .onCreate((snap, context) => {
    // Get an object representing the document
    // e.g. {'name': 'Marie', 'age': 66}
    let now = new Date();
    let specialty = snap.data()['specialty'];
    let motifOperation = snap.data()['motif'];
    let typeOperation = snap.data()['type'];
    let agentsOnOperation = snap.data()['agentsOperation'];

    let year = now.getFullYear();

    console.log("data : " + motifOperation);

    addOperationStats(motifOperation, specialty, year, typeOperation)

    addAgentStats(agentsOnOperation, specialty, year, typeOperation);

  });

function addOperationStats(motifOperation, specialty, year, typeOperation) {
  let count = 0;

  /**
   * Push stats Operation
   * By Motif : foreach INTERVENTION, we increment count with motif key.
   * Ex: AVP: 18
   */
  let doc = db.collection(COLLECTION_STATISTIQUES).doc(year.toString());

  if (typeOperation == MOTIF_INTERVENTION) {
    doc
      .get()
      .then(operationStat => {
        if (!operationStat.exists) {
          console.log("No operation actually present - count does not exist");
          let obj = {};
          let motifsMap = {};
          obj[motifOperation] = count + 1;
          motifsMap[specialty] = obj;
          console.log("count - obj : " + JSON.stringify(motifsMap));
          doc
            .set(motifsMap, { merge: true });
        } else {
          console.log("count - data : " + JSON.stringify(operationStat.data()));
          let motifs = operationStat.data()[specialty];
          if (motifs !== undefined) {
            console.log("count - motifs : " + JSON.stringify(motifs));
            for (var motif of Object.keys(motifs)) {
              if (motifOperation == motif) {
                count = motifs[motif];
              }
            }
          }
          let obj = {};
          let motifsMap = {};
          obj[motifOperation] = count + 1;
          motifsMap[specialty] = obj;
          console.log("count - obj : " + JSON.stringify(motifsMap));
          doc
            .set(motifsMap, { merge: true });
        }
      });
  }
}

function addAgentStats(agentsOnOperation, specialty, year, typeOperation) {
  /**
       * Push stats Agents
       */
  agentsOnOperation.forEach(agent => {
    console.log("Doc " + specialty);
    console.log("Doc " + year.toString());
    console.log("Doc " + agent.id);
    console.log("Doc " + typeOperation);

    let countType = 0;
    let countTime = 0;

    let docAgent = db.collection(COLLECTION_STATISTIQUES)
      .doc(year.toString())
      .collection(agent.id)
      .doc(specialty);

    return docAgent
      .get()
      .then(agentStat => {
        if (!agentStat.exists) {
          console.log("No agent actually present - count does not exist");
        } else {
          console.log("count - agent : " + JSON.stringify(agentStat.data()));
          let types = agentStat.data()['type'];
          console.log("count - types : " + JSON.stringify(types));
          for (var type of Object.keys(types)) {
            if (typeOperation == type) {
              countType = types[type];
              break;
            }
          }
          let times = agentStat.data()['time'];
          console.log("count - times : " + JSON.stringify(times));
          for (var time of Object.keys(times)) {
            if (typeOperation == type) {
              countTime = times[time];
              break;
            }
          }

        }
        let objType = {};
        let objTime = {};
        let map = {};
        objType[typeOperation] = countType + 1;
        objTime[typeOperation] = countTime + agent.time;
        map['type'] = objType;
        map['time'] = objTime;
        console.log("count - obj : " + JSON.stringify(map));
        docAgent
          .set(map, { merge: true });
      })
  });
}