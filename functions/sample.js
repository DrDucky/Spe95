const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

const firebase = require('firebase');
// const maxInOutsInEvent = 5;

const express = require('express');

const app = express();

app.use(express.static('public/remote_access_counters'));
var path = require('path');
const Jimp = require('jimp');
const fs = require('fs')
 

const NB_CHUNKS = 30;

const COLLECTION_EVENT_META_DATA = "eventMetaData";
const COLLECTION_MESSAGES = "messages";
const COLLECTION_HISTORY_CHUNKS = "historyChunks";
const COLLECTION_USER_EXTRA = "userExtra";
const COLLECTION_PURCHASES_HISTORY = "purchasesHistory";
const COLLECTION_PURCHASES_HISTORY_TEMP = "purchasesHistoryTemp";
const COLLECTION_COUNTER_ONLY = "counterOnly";
const COLLECTION_SECURITY = "security";
const COLLECTION_SCHEDULERS = "schedulers";
const COLLECTION_REMOTE_ACCESS = "remoteAccess";
const COLLECTION_PURCHASABLE_PRODUCTS = "purchasableProducts";
const COLLECTION_ADMIN_STATS = "adminStats";
const COLLECTION_ADMIN_SUBSCRIPTIONS = "adminSubscriptions";

const MAX_CONTRIBUTORS_PAID = 20;
const MAX_CONTRIBUTORS_FREE = 2;

const PUSH_MODE_LEGACY = 1;
const PUSH_MODE_NEW = 2;
const SEND_WELCOME_EMAIL = true;

const pushMode = PUSH_MODE_NEW;
const SECURITY_CHECK_SUPPORTED = false;
const REMOTE_ACCESS_IN_MAINTENANCE = false;
const AUTOMATIC_REPORT_SUPPORTED = true;
const AUTOMATIC_REPORT_LIVE = true;

const concurrentCounters = [];

concurrentCounters["free"] = 1;
concurrentCounters["subscription.full.monthly_10_eur"] = 2;

Date.prototype.toShortDateFormat = function(gmtOffset, language) {

    let monthNames;
    
    if (language === 'en') {
       monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    } else if (language === 'fr') {
       monthNames = ["jan", "fév", "mar", "avr", "mai", "Jun", "jul", "aoû", "sep", "oct", "nov", "dec"];
    }
    
   let localDate = new Date(this.getTime() + (gmtOffset * 60 * 60 * 1000));

    let day = localDate.getDate();
    
    let monthIndex = localDate.getMonth();
    let monthName = monthNames[monthIndex];
    
    let year = localDate.getFullYear();
    
    return `${day} ${monthName} ${year}`;  
}

Date.prototype.toTimeFormat = function(gmtOffset, is24HourFormat) {
   let localDate = new Date(this.getTime() + (gmtOffset * 60 * 60 * 1000));
   
   let h = localDate.getHours();
   let m = localDate.getMinutes();

   if (is24HourFormat) {
      let hh = ("0" + h).slice(-2);
      let mm = ("0" + m).slice(-2);
   
      return `${hh}:${mm}`;
   }
   else {
      // Source: https://en.wikipedia.org/wiki/12-hour_clock
      let amPm;
      if (h === 0) {
         h = 12;
         amPm = " am";
      }
      else
      if (h === 12) {
         // Don't need to set it to 12, because in the 'if' clause. h = 12;
         amPm = " pm";
      }
      else
      if (h > 12) {
         h -= 12;
         amPm = " pm";
      } else {
         amPm = " am";
      }
       
      let hh = ("0" + h).slice(-2);
      let mm = ("0" + m).slice(-2);
    
      return `${hh}:${mm}${amPm}`;
   }
}

//
//const planMaxHits = {};
//
//const globalTrigger = buildGlobalVariables();
//
//function buildGlobalVariables() { 
//   return db.collection(COLLECTION_PURCHASABLE_PRODUCTS)
//      .get()
//      .then(matches => {
//         if (matches.empty) { 
//            console.log("ERROR: Could not get the purchasableProducts collection");
//            return null;
//         } else {
//            // let result = { };
//            matches.forEach(match => {
//               let plan = match.data().id;
//               let maxHits = match.data().maxHits;
//               planMaxHits[plan] = maxHits;
//            });
//
//            console.log("planMaxHits=" + JSON.stringify(planMaxHits));
//            return "done";
//         }
//      });
//   }
//   
//exports.onPurchasableUpdate = functions.firestore.document(COLLECTION_PURCHASABLE_PRODUCTS + '/{pp}').onUpdate((snap, context) => {
//   return buildGlobalVariables();
//});


const i18n_en = {
   user_not_found: "User not found",
   number_of_concurrent_events_quota_exceeded_paid: "Concurrent counters quota exceeded. You have more than 2 active counters. Please insure you don't have overlaps in your scheduled counters, or live counters already running. Please change trigger time or delete one of them",
   number_of_concurrent_events_quota_exceeded_free: "Concurrent counters quota exceeded. You have more than 1 active counter. Delete it before creating a new one",
   event_error: "Generic error, could not complete the action",
   event_not_found: "Counter not found, operation could no t complete.",
   same_account_used_multiple_times: "We have detected this account has been used by multiple users.This action cannot complete",
   number_of_contributors_quota_exceeded_paid: "The maximum number of contributors has been reached (20 max). The owner should remove contributors from the list.",
   number_of_contributors_quota_exceeded_free: "The maximum number of contributors has been reached (2 max). The owner should remove contributors from the list.",
   scheduled_counter_generic_failure: "Could not create your scheduled counter",
   scheduler_subject_error: "Shared counter app: failure",
   scheduler_subject_success: "Shared counter app: counter created",
   scheduler_message_success: "The scheduled counter was created, and an email with a link has been sent to each of the selected contributors.\n\nThank you for using Shared Counter.\n\nBest regards,\n\nChristophe",
   remote_access_error: "Invalid user account or counter group",
   remote_access_in_maintenance: "In maintenance"
};

const i18n_fr = {
   user_not_found: "Utilisateur non trouvé",
   number_of_concurrent_events_quota_exceeded_paid: "Nombre maximum de compteurs actifs est atteint. Vous ne pouvez pas en avoir plus de 2 en même temps. Veuillez vérifier si vous avez des recouvrements de compteurs dans vos planifications, et changer l'heure de création ou supprimer l'un d'eux.",
   number_of_concurrent_events_quota_exceeded_free: "Nombre maximum de compteurs actifs est atteint pour la version gratuite. Vous ne pouvez pas en avoir plus de 1 en même temps. Si ce compteur a été créé grâce au planificateur, veuillez décaler son horaire de lancement, ou supprimer l'autre compteur actuellement actif",
   event_error: "Erreur générique, l'opération n'a pas pu aboutir.",
   event_not_found: "Compteur non-trouvé. L'opération n'a pas pu aboutir.",
   same_account_used_multiple_times: "Nous avons détecté que ce compte est utilisé par plusieurs personnes. L'action n'a pas pu aboutir.",
   number_of_contributors_quota_exceeded_paid: "Le nombre maximum de contributeurs a été atteint (20 max). Il faut supprimer des contributeurs pour en ajouter d'autres.",
   number_of_contributors_quota_exceeded_free: "Le nombre maximum de contributeurs a été atteint (2 max). Il faut en supprimer un pour pourvoir le remplacer.",
   scheduled_counter_generic_failure: "La planification de votre compteur a échoué",
   scheduler_subject_error: "Compteur partagé: échec.",
   scheduler_subject_success: "Compteur paratagé: compteur créé.",
   scheduler_message_success: "Le compteur planifié a été créé, et un email contenant un lien a bien été envoyé à chacun des contributeurs que vous avez sélectionnés.\n\nMerci pour l'utilisation de l'application Compteur partagé.\n\nCordialement,\n\nChristophe",
   remote_access_error: "Utilisateur ou groupe invalide",
   remote_access_in_maintenance: "En maintenance"
};

const i18n = {
   en: i18n_en,
   fr: i18n_fr
};

function getI18n(language, key) { 
   let la = language || 'en';
   let tb = i18n[la] || i18n_en;
   
   return tb[key];
}

const DEFAULT_BC = "WHITE";
const DEFAULT_TC = "BLACK";
const DEFAULT_TS = "32";
const DEFAULT_LA = "en";

// URL FORMAT is like this:
// https://people-count-37e8a.web.app/public?ownerId={theOwnerId}&rai={theRaiCodeDefinedByTheCustomForAParticularCounter}&bc={WHITE|BLACK}&tc={WHITE|BLACK}&ts={8|16|32|64|128}
//
// ex:
// https://people-count-37e8a.web.app/public?ownerId=0ddLSKcHF7YFz4kT2uzjQ2hHEU22&rai=TUTU&h=50&tc=WHITE&bc=BLACK&ts=16
app.get('/public', (request, response) => {
   
   response.set('Cache-control', 'public, max-age=30, s-maxage=30');
   
   let remoteAccessId = request.query.rai;
   let ownerId = request.query.ownerId;
   let w = request.query.w || 300;
   let h = request.query.h || 100;
   let bc = request.query.bc || DEFAULT_BC;
   let tc = request.query.tc || DEFAULT_TC;
   let ts = request.query.ts || DEFAULT_TS;
   let la = request.query.la || DEFAULT_LA;
   let font;

   let textToWrite;
   if (w > 400) {w = 400;} else if (w <= 0) { w = 300;}
   if (h > 100) {h = 100;} else if (h <= 0) { h = 100;}

   return db.collection(COLLECTION_REMOTE_ACCESS)
      .where("ownerId", "==", ownerId)
      .where("rai", "==", remoteAccessId)
      .limit(1)
      .get()
      .then(matches => {
         if (matches.empty) {
            console.log(`No match found for ownerId=${ownerId} and remoteAccessId=${remoteAccessId}`);
            return null;
         } else {
            let recordData = matches.docs[0].data();            
            console.log("Remote access record found - content =" + JSON.stringify(recordData));
            return recordData;
         }
      })
      .then(match => {
         // TODO: would be best to have this check ealier, to avoid looking for a record, while in maintenance.
         if (REMOTE_ACCESS_IN_MAINTENANCE === true) {
            textToWrite = getI18n(la, remote_access_in_maintenance);
            ts = "16";            
         }
         else if (match === null) {
            textToWrite = getI18n(la, "remote_access_error");
            ts = "16";
         } else {
            textToWrite = `${match.counter}`;
         }
         return textToWrite;
      })
      .then(textToWrite => {
         // Right, now, just need to display the text in the image. First, load the right font
         console.log("Should add text on image :" + textToWrite);

         // Sanity checks on input params
         // textSize (ts) must belong to the table below.
         let possibleTextSizes = ["8", "16", "32", "64", "128"];
         if (possibleTextSizes.indexOf(ts) < 0) {
            ts = "32";
         }

         if (bc !== 'BLACK' && (bc !== 'WHITE')) {
            console.log("Color is an hexadecimal color, and value is:" + bc);
            bc = parseInt(bc, 16) /*& 0xFFFFFF */;
         }

         console.log(`ts=${ts}, tc=${tc}, bc=${bc}, w=${w}, h=${h}`);

         // Once we have both text size and color, we can get the right font to use.
         font = Jimp[`FONT_SANS_${ts}_BLACK`];

         return Jimp.loadFont(font);
      })
      .then(fontLoaded => {
         console.log("font name is:" + font);
         let textW = Jimp.measureText(fontLoaded, textToWrite); // width of text
         let textH = Jimp.measureTextHeight(fontLoaded, textToWrite); // height of text
         
         console.log(`dimensions: textW=${textW} textH=${textH}`);

         // First, create a colored background layer
         let imageBck = new Jimp(w, h, bc, (err, imageBck) => {
            if (err) {
               throw err
            }
         })
         
         // Then a transparent one, same size as previous one.
         let textLayer = new Jimp(w, h, 0x0, (err, textLayer) => { 
            if (err) {
               throw err
            }
         })
         
         // On this transparent background, we use the black text
         textLayer.print(
            fontLoaded, 
            (w - textW) / 2,
            (h - textH) / 2, 
            {  text: textToWrite, 
               alignmentX: Jimp.HORIZONTAL_ALIGN_CENTER, 
               alignmentY: Jimp.VERTICAL_ALIGN_MIDDLE
            },
            textW,
            textH);

         // And apply an XOR with the desired text color. Transparent layer will contain the text wit hthe right color
         textLayer.color([{ apply: 'xor', params: [tc] }]); // red text

         // Then merge the text layer on top of background layer.
         imageBck.composite(textLayer, 0, 0);

         // Et voil!
         return imageBck
      })
      .then(image => {
         console.log("Image is built");
         return image.getBufferAsync(Jimp.MIME_PNG);
      })
      .then(buf => {
            console.log("Returning image to client");
            response.writeHead(200, {
               'Content-Type': "image/png",
               'Content-Length': buf.length
            });
            
            response.end(Buffer.from(buf, 'base64'));
            
            return 0;
         });
});

exports.app = functions.https.onRequest(app);

exports.scheduledFunction = functions.pubsub.schedule('0 * * * *').onRun((context) => {
   let now = new Date();
   let h = now.getHours();
   let d = now.getDay();
   let hourOfWeek = (d * 24) + h;

   console.log("(auto) - should shoot active schedulers planned for week hour = " + hourOfWeek);
   return checkShedulersForHourOfWeek(hourOfWeek)
   .then(() => {
      return buildAdminStats();
   });
});

exports.dailyFunction = functions.pubsub.schedule('0 0 * * *').onRun((_context) => {
   return buildAdminSubscriptionsStats();
});


function buildAdminSubscriptionsStats() {
   return db.collection(COLLECTION_USER_EXTRA)
      .where("plan", ">", "subscription")
      .get()
      .then(paidUsers => {
         if (paidUsers.isEmpty) {
            console.log("No user with 'subscription*' plan found");
            return null;
         }
         else {
            // Amongst the matches, find out the still active, and soon to leave users.
            console.log("HAs found users with 'subscription*' plan");

            let subscriptionsDocument = { active:0, soonToLeave: 0};
            let activeStates = [1, 2, 4, 6, 7, 8, 9];
            paidUsers.forEach(user => {
               let data = user.data();
               let lastNotificationType = data.lastNotificationType;

               console.log("Checking this user with lastNotificationType=" + lastNotificationType);
               if (activeStates.indexOf(lastNotificationType) >= 0) {
                  subscriptionsDocument.active++;
               } else {
                  subscriptionsDocument.soonToLeave++;
               }
            });

            return subscriptionsDocument;
         }
      })
      .then(subscriptionsDocument => {
         if (subscriptionsDocument === null) {
            return null;
         } else {
            // Now, we add the record.
            let docName = dateToFormat(new Date());
            subscriptionsDocument.at = docName;
            
            return db.collection(COLLECTION_ADMIN_SUBSCRIPTIONS)
               .doc(docName)
               .set(subscriptionsDocument);
         }
      })

      ;
}

function buildAdminStats() {
   // entry to add.
   let entry = null;
   
   // Now, look for all active events, which means:
   // end > now
   // considering end = start + 24h
   // start > (now - 24h)

   let now = new Date();
   let statsDocumentName = dateToFormat(now);

   let createdAfter = new Date();
   createdAfter.setHours(now.getHours() - 24);
   createdAfter.setMinutes(0);
   createdAfter.setSeconds(0);
   createdAfter.setMilliseconds(0);

   return db.collection(COLLECTION_COUNTER_ONLY)
      .where("createdAt", ">", createdAfter)
      .get()
      .then(matches => {
         if (matches.isEmpty) {
            // no match
            return null;
         }
         else {
            entryFree = { abs:0, counter: 0, hits: 0, nbCounters: 0};
            entryPaid = { abs:0, counter: 0, hits: 0, nbCounters: 0};

            // Now, loop for the useful data, and accumulate to the entry.
            matches.forEach(match => {
               let one = match.data();
               
               if (one.ownerPlan === "free") {
                  entryFree.abs += one.abs;
                  entryFree.counter += one.counter;
                  entryFree.hits += one.hits;
                  entryFree.nbCounters++;
               } else {
                  entryPaid.abs += one.abs;
                  entryPaid.counter += one.counter;
                  entryPaid.hits += one.hits;
                  entryPaid.nbCounters++;
               }
            });

            // This is what we return
            entry = { };
            entry.free = entryFree;
            entry.paid = entryPaid;

            entry.absTotal = entryFree.abs + entryPaid.abs;
            entry.counterTotal = entryFree.counter + entryPaid.counter;
            entry.hitsTotal = entryFree.hits + entryPaid.hits;
            entry.nbCounters = matches.size;
            entry.hourOfDay = now.getHours();

            return entry;
         }
      })
      .then(entry => {
         if (entry === null) {
            return null;
         }
         else {
            // Ok, we have the entry. Find the entry in the collection for the given date, and append this entry to it.
            return db.collection(COLLECTION_ADMIN_STATS).doc(statsDocumentName).get();
         }
      })
      .then(doc => {
         if (doc === null) {
            return null;
         }
         else {
            let records = [];

            if (doc.exists) {
               let data = doc.data();
               
               records = data.records || [];
            }
         
            records.push(entry);
            
            // Then the various totals
            let absTotal = entry.absTotal;
            let hitsTotal = entry.hitsTotal;
            let counterTotal = entry.counterTotal;

            return db.collection(COLLECTION_ADMIN_STATS)
               .doc(statsDocumentName)
               .set({
                     records: records,
                     absTotal: absTotal,
                     hitsTotal: hitsTotal,
                     counterTotal: counterTotal }, {
                     merge: true
                  });
         }
      })
      ;
   }

function dateToFormat(date) {
   let y = date.getFullYear();
   let m = date.getMonth() + 1;
   let d = date.getDate();
   
   let mm = ("0" + m).slice(-2);
   let dd = ("0" + d).slice(-2);

   return `${y}-${mm}-${dd}`;
}

exports.triggerWeeklyReport = functions.pubsub.schedule('every sunday 01:01')
   .timeZone('Europe/Paris')
   .onRun((context) => {
      return doTriggerReport(7, false);
});

function doTriggerReport(period, debug) {
   if (AUTOMATIC_REPORT_SUPPORTED === false) {
      console.log("Will NOT build daily report");
      return 0;
   }
   
   console.log(`Will build report for last ${period} day(s)`);
   
   let toDate = new Date();
   
   let fromDate = new Date();
   fromDate.setSeconds(0);
   fromDate.setMilliseconds(0);
   fromDate.setHours(fromDate.getHours() - (period * 24));
   
   let metaDatas = [];
   let eventIds = [];
   let counterOnlys = [];
   let ownersMap = {};
   
   return db.collection(COLLECTION_EVENT_META_DATA)
      .where("expiresOn", ">=", fromDate)
      .where("expiresOn", "<", toDate)
      .where("ownerPlan", "==", "subscription.full.monthly_10_eur")
      .get()
      .then(toBeReported => { 
         if (toBeReported.isEmpty) {
            console.log(`No reports found for last ${period} day(s)`);
            return null;
         }
         else {
            toBeReported.forEach(match => {
               let emd = match.data();
               if (emd.owner !== undefined) {
                  metaDatas.push(emd);
                  eventIds.push(emd.eventId);
               } else {
                  console.log(`Event ${emd.eventId} removed from reports because has no owner.`);
               }
            });
            return metaDatas;
         }
      })
      .then(matches => {
         if (matches === null || matches.length === 0) {
            console.log("returning null for previous reasons - 1");
            return null;
         }
         else {
            // Here, we want the list of counterOnly matching all event ids.
            let promises = [];
            eventIds.forEach(eventId => {
               promises.push(db.collection(COLLECTION_COUNTER_ONLY).doc(eventId).get());
            });
            return Promise.all(promises);
         }
      })
      .then(matches => {
         if (matches === null) {
            console.log("returning null for previous reasons - 2");
            return null;
         }
         else if (matches.empty) {
            console.log("empty!!");
            return null;
         }
         else {
            console.log("We have the counter only now:" + matches.length);
            matches.forEach(match => {
               counterOnlys.push(match.data());
            });
            return counterOnlys;
         }
      })
      .then(matches => {
         if (matches === null) {
            console.log("returning null for previous reasons - 3");
            return null;
         } 
         else {
            console.log(`Found ${metaDatas.length} reports to build`);
            
            let ownersPromises = [];
            metaDatas.forEach(match => { 
               // owners.push(match.owner);
               ownersPromises.push(getDetailsForUserId(match.owner));
            });
            
            /*// Get all owners details
            return db.collection(COLLECTION_USER_EXTRA)
               .where('userId', 'in', owners)
               .get();
               */
            return Promise.all(ownersPromises);
         }
      })
      .then(owners => {
         if (owners === null) {
            console.log("returning null for previous reasons - 4");
            return null;
         }
         else 
         if (owners.empty) {
            console.log("Owners not found");
            return null;
         }
         else {
            console.log("We have the owners");
            owners.forEach(owner => {
               ownersMap[owner.userId] = owner;
            });
            return ownersMap;
         }
      })
      .then(allReady => {
         if (allReady === null) {
            console.log("returning null for previous reasons - 5");
            return null;
         }
         else {
            console.log("We have the owners, we have the metadata, we also have the counter only. Should shoot the report emails");
            
            // index, Date, title, nb of contributors, max value, hour of max value
            let emailToSend = [];
            for (var ownerId in ownersMap) {
               let dataForReport = {
                  owner: ownersMap[ownerId],
                  metaDatas: metaDatas.filter( md => md.owner === ownerId).sort(chronologicalSortCreatedAt),
                  counterOnlys: counterOnlys.filter( md => md.owner === ownerId).sort(chronologicalSortCreatedAt)
               };
               emailToSend.push(buildReport(period, dataForReport, debug));
            }

            return Promise.all(emailToSend);
         }
      })
      ;
}

function chronologicalSortCreatedAt(a, b) {
   return a.createdAt < b.createdAt;
}

function buildReport(period, data, debug) {
   
   
   // data = {
   //   owner => details of the ownerData
   //   metaDatas => array of metadata for this owner   
   //   counterOnlys => array of counter only elements, in the same order.
   // }
   
   if (data.owner.email !== undefined && data.owner.email.length > 0) {
      try {
         console.log("Shooting email with data:" + JSON.stringify(data));
         
         let lines = "";
         let gmtOffset = data.owner.gmtOffsetInHours || 0;
         let is24HourFormat = data.owner.is24HourFormat || true;
         let language = data.owner.language || 'en';
         let validLine = 0;
         for (var i = 0; i < data.metaDatas.length; i++) {
            let maxValue = data.counterOnlys[i].maxCounter || 0;
            
            if (maxValue > 0) {
               validLine++;
               
               let createdAt = data.metaDatas[i].createdAt.toDate();
               let formatedCreatedAt = createdAt.toShortDateFormat(gmtOffset, language);
               let title = data.metaDatas[i].title;
               let maxValueHour;
               let maxTimeStamp = data.counterOnlys[i].maxCounterTimeStamp;
            
               if (maxTimeStamp !== undefined && maxTimeStamp !== null) {
                  maxValueHour = maxTimeStamp.toDate().toTimeFormat(gmtOffset, is24HourFormat);
               } else {
                  maxValueHour = "-";
               }

               //let line = `<tr><td style="text-align: center;padding:10px">${formatedCreatedAt}</td><td style="text-align: center;padding:10px">${title}</td><td style="text-align: center;padding:10px">${maxValue}</td><td style="text-align: center;padding:10px">${maxValueHour}</td></tr>`; 
               let line = `<tr><td style="text-align: center;">${formatedCreatedAt}</td><td style="text-align: center;">${title}</td><td style="text-align: right;">${maxValue}</td><td style="text-align: center;">${maxValueHour}</td></tr>`; 
            
               lines += line;
            }
         }
         
         console.log("Table lines:" + lines);
         
         if (validLine > 0) {
            let templateName = `report_${period}_${language}`;
         
            let template = {
               name: templateName,
               data: {
                  displayName: data.owner.displayName || "",
                  lines: lines
               }
            };
         
            if (AUTOMATIC_REPORT_LIVE === false || debug === true) {
               return sendEmailTo(null, template, true);
            
            } else {
               return sendEmailTo(data.owner, template, true);
            }
         }
         else {
            return Promise.resolve(true);
         }
         
      } catch(e) {
         console.error(e);
         return Promise.resolve(true);
      }
   }
   else {
      // Actually prepare the mail to send.
      console.log(`user ${data.owner.userId} has no email. Skipping report`);
      return Promise.resolve(true);
   }
}

// Called when the hour of week changes, or when debug data changes.
function checkShedulersForHourOfWeek(hourOfWeek) {
   console.log("Should shoot active schedulers planned for week hour = " + hourOfWeek);
   return db.collection(COLLECTION_SCHEDULERS)
      .where("active", "==", true)
      .where("hoursOfWeek", "array-contains", hourOfWeek)
      .get()
      .then(schedulers => {
         let sc = [];
         if (schedulers.empty) {
            console.log("No scheduler found for hourOfWeek=" + hourOfWeek);
            return sc;
         } else {
            schedulers.forEach(match => {
               console.log("Should shoot invitation emails for sheduler=" + match.data().name);
               sc.push(match.data());
            });
         }
         return sc;
      })
      .then(schedulers => {
         console.log("Number of schedulers for this time:" + schedulers.length);
         let promises = [];

         schedulers.forEach(scheduler => {
            promises.push(createEventAndSendEmails(scheduler));
         })

         return Promise.all(promises);
      });
}

function createEventAndSendEmails(scheduler) {
   let newEvent;
   let overallSuccess = false;
   let ownerData;
   let recipientsData;
   
   return createScheduledEvent(scheduler)
     .then(createdEvent => {
        // We have the event, we can fetch the mails
        newEvent = createdEvent;

        if (newEvent.success === true) {
           console.log("Sheduled event created. All's good");
        } else {
           console.log("Scheduled event creation failure; Reason=" + createdEvent.reason);
        }

        return createdEvent;
     })
     .then(createdEvent => {
        if (createdEvent.success === true) {
           console.log("Now fetching all emails");
           return getRecipiendDetailsForScheduler(scheduler);
        } else {
           console.log("NOT fetching the emails, because creation failed");
           return Promise.resolve(null)
        }
     })
     .then(recipientDetails => {
        if (recipientDetails !== null) {
           console.log("We have the event, and the repicipient details, send all - " + recipientDetails.length);
           recipientsData = recipientDetails;
           return scheduledEventSendEmailTo(newEvent, recipientDetails, scheduler);
        } else {
           return false;
        }
     })
     .then(success => { 
        overallSuccess = success;
        
        if (success === true) {
           return getDetailsForUserId(scheduler.userId)
        } else {
           return null;
        }
     })
     .then(ownerDetails => {
        if (ownerDetails === null) {
           console.log("user NOT found, will not continue to send status email");
           overallSuccess = false;
           return null;
        }
        
        ownerData = ownerDetails;
        
        let template;

        if (overallSuccess === true) {
           // subject = i18n[scheduler.emailLanguage].scheduler_subject_success;
           // message = i18n[scheduler.emailLanguage].scheduler_message_success;
           
           let templateName = "scheduler-success-feedback-" + scheduler.emailLanguage;
           
           template = {
              name: templateName,
              data: {
                 name: ownerDetails.displayName,
                 deeplink: "https://counter.cdb.fr/events/" + newEvent.eventId + "/join"
              }
           };
        } else {
           let reason;
           //subject = i18n[scheduler.emailLanguage].scheduler_subject_error;
           try {
              console.log("Failure reason=" + newEvent.reason);
              reason = i18n[scheduler.emailLanguage][newEvent.reason];
           } catch(e) {
              reason = i18n[scheduler.emailLanguage].scheduled_counter_generic_failure;
           }
           
           let templateName = "scheduler-failure-feedback-" + scheduler.emailLanguage;
           
           template = {
              name: templateName,
              data: {
                 name: ownerDetails.displayName || "",
                 reason: reason
              }
           };
        }
        
        return sendEmailTo(ownerDetails, template, true);
     })
     .then(done => {
        if (overallSuccess) {
           console.log("Yes, will send push notifications");
           // Send a push notif to everyone.
           var payload = {
              data:{
                 action: "newScheduledCounter",
                 content: "A new counter is ready for you",
                 deeplink: "https://counter.cdb.fr/events/" + newEvent.eventId + "/join",
                 fromId: scheduler.userId,
                 fromDisplayName: ownerData.displayName
              }
           };

           let photoURL = ownerData.photoURL;
           if (photoURL !== undefined && photoURL !== null) {
              payload.data.fromPhotoURL = photoURL;
           }

           recipientsData.forEach( recipientData => {
              var pushToken = recipientData.pushToken;

              if (pushToken !== undefined && pushToken !== null) {
                 console.log("Should send push to pushToken=" + pushToken);
                 admin.messaging().sendToDevice(pushToken, payload);
              }
           });
        } else {
           console.log("Push notif not send because overallSuccess is not true:");
        }
        
        return 0;
     })
}

function createScheduledEvent(scheduler) {
   let userId = scheduler.userId;
   let userRef = db.collection(COLLECTION_USER_EXTRA).doc(userId);
   let maxValue = scheduler.maxValue;
   let remoteAccessId = scheduler.remoteAccessId;
   let gmtOffsetInHours = scheduler.gmtOffsetInHours;
   let group = scheduler.group || 0;
   let now = new Date();

   let d = now.getDate();
   let m = now.getMonth() + 1;

   let title = scheduler.name + " (" + d + "/" + m + ")";
   let security; // Left undefined on purpose, there is no security checks on scheduled event creation.

   console.log("remote access id= " + remoteAccessId);
   
   return checkAndCreateEvent(userId,
      userRef,
      maxValue,
      title,
      security,
      remoteAccessId,
      gmtOffsetInHours,
      group,
      "scheduler");
}

function scheduledEventSendEmailTo(createdEvent, emails, scheduler) {
   let eventId = createdEvent.eventId;
   console.log("Now, sending emails to all contributors, with link having:" + eventId);
   
   let sendEmailPromises = [];
   emails.forEach(email => {
      sendEmailPromises.push(sendShedulerContributorEmail(createdEvent, email, scheduler));
   });
   
   return Promise.all(sendEmailPromises)
      .then(done => { 
         return true; 
   });
}

function sendShedulerContributorEmail(createdEvent, userDetails, scheduler) {
   
   let templateName = "scheduler-success-invitation-" + scheduler.emailLanguage;
   let deeplink = "https://counter.cdb.fr/events/" + createdEvent.eventId + "/join";
   console.log("Deeplink:" + deeplink);
   
   let template = {
      name: templateName,
      data: {
         name: userDetails.displayName,
         customMessage: scheduler.customMessage,
         deeplink: deeplink
      }
   };

   return sendEmailTo(userDetails, template, false)
}


function getRecipiendDetailsForScheduler(scheduler) {
   let userPromises = [];
   let allUsersDetails = [];

   // First load user extra for each.
   scheduler.contributorIds.forEach(userId => {
      userPromises.push(
         db.collection(COLLECTION_USER_EXTRA)
            .doc(userId)
            .get()
            .then(userExtra => {

               if (userExtra.exists) {
                  console.log("Found a user, getting details");
                  allUsersDetails.push({
                     email: userExtra.data().email, 
                     userId: userExtra.data().userId,
                     displayName: userExtra.data().displayName,
                     photoURL: userExtra.data().photoURL,
                     pushToken: userExtra.data().pushToken
                  });
               } else {
                  console.log("could not find user details");
               }

               return 0;
            })
         );
   });

   return Promise.all(userPromises)
      .then(done => {

         // Then, add the ones we have the email only.
         scheduler.contributorsEmailsOnly.forEach(email => {
            allUsersDetails.push({
               email: email,
               displayName: ""
            });
         });

         allUsersDetails.forEach(x => {
            console.log("Will send to:" + x.email + ", user is:" + x.userId);
         });
         return allUsersDetails;
      });
}


function getDetailsForUserId(userId) {
   return db.collection(COLLECTION_USER_EXTRA)
      .doc(userId)
      .get()
      .then(userExtra => {
         if (userExtra.exists) {
            let user = userExtra.data();
            
            user.language = user.language || 'en';
            user.gmtOffsetInHours = user.gmtOffsetInHours || 0;
            user.is24HourFormat = user.is24HourFormat || true;
            
            return user;

         } else {
            return null;
         }
   });
}

function sendEmailTo(userDetails, template, copyMe) {
   
   let emailData = {
      template: template
   };
   
   if (userDetails !== null) {
      emailData.to = [ userDetails.email ];
   }
   
   if (copyMe === true) {
      emailData.bcc = [ "info@shared-counter.fr" ];
   }
   
   return db.collection('mail')
      .add(emailData)
      .then(sent => {
         console.log('Queued email for delivery!');
         return 200;
      });
}


exports.helloPubSub = functions.pubsub.topic('purchaseNotifications').onPublish((message) => {

   // cf: https://firebase.google.com/docs/functions/pubsub-events
   console.log("PURCHASE_TRACE - In pub/sub purchaseNotifications!!!");

   // Decode the PubSub Message body.
   // console.log("PURCHASE_TRACE, attributes=" + message);

   const messageBody = message.data ? Buffer.from(message.data, 'base64').toString() : null;
   console.log(`Hello money!!! ${messageBody || 'World'}`);

   let body = JSON.parse(messageBody);
   let successCode = 200;
   let errorCode = -1;

   if (pushMode ===  PUSH_MODE_NEW) {
      let purchaseToken = body.subscriptionNotification.purchaseToken;

      let timeOfEvent = new Date(Number(body.eventTimeMillis));
      let entry = {
         purchaseToken: purchaseToken,
         body: body,
         date: timeOfEvent
      };

      return db.collection(COLLECTION_PURCHASES_HISTORY_TEMP)
         .add(entry)
         .then(added => {
             return successCode;
       });
   }
   else {

      // Need to lookup in COLLECTION_PURCHASES_HISTORY the entry that already has this token.
      let purchaseToken = body.subscriptionNotification.purchaseToken;
      console.log("Looking for entry that has purchaseToken:" + purchaseToken);

      return db.collection(COLLECTION_PURCHASES_HISTORY)
        .where("purchaseToken", "==", purchaseToken)
        //.limit(1)
        .get()
        .then(matches => {

          if (matches.empty) {
            console.log("PURCHASE_TRACE - No document with such purchaseToken. Adding in TEMP:" + purchaseToken);

            // The purchaseToken could not be found. It means we got the pubsub notification before the mobile was able to create it. This happens sometimes...
            // The strategy is: we store these data in a COLLECTION_PURCHASES_HISTORY_TEMP collection. Also, we have a listening on COLLECTION_PURCHASES_HISTORY
            // creation. When it will be fired, we'll then loop up in the _TEMP collection, and try to reassemble (insert) the data we just received here.
            // The code below is about storing these data.
            return db.collection(COLLECTION_PURCHASES_HISTORY_TEMP)
               .add({body: body, // Body contains everything
                  purchaseToken: purchaseToken}) // Will be used as a search key.
               .then(added => {
                 sendEmail("sharedCounterNotif - Notif: token not found",
                        "notificationType=" + body.subscriptionNotification.notificationType
                         + "<br>for token=" + purchaseToken);

                 return errorCode;
               });
          }

          // Now, need to update whatever needs to. Including, status. Status should be also reflected on the corresponding user.
          // We loop, but should be 1 only.
          let entryData = matches.docs[0].data();

          let actions = entryData.actions || [];
          let userId = entryData.userId;

          let now = new Date();
          let action = {
            notificationType: body.subscriptionNotification.notificationType,
            packageName: body.packageName,
            date: now
          };
          actions.push(action);

          return matches.docs[0]
            .ref
            .set({actions: actions,
                 lastNotificationType : body.subscriptionNotification.notificationType },
                {merge: true})
            .then(x => {
               // Update the user extra accordingly.
               console.log("PURCHASE_TRACE - " + COLLECTION_PURCHASES_HISTORY + " updated with purchase data");

               let plan = "";
               let hasRemoteAccessEnabled = false; // By default, no remote access allowed.

               return db.collection(COLLECTION_USER_EXTRA)
                 .doc(userId)
                 .get()
                 .then(user => {
                   if (user.exists) {
                      console.log("PURCHASE_TRACE - user found - updating with purchase data");

                      // Now, updates its subscription status.
                      let userData = user.data();

                      let str = "";
                      switch(body.subscriptionNotification.notificationType) {
                         case 1: str = "(1) SUBSCRIPTION_RECOVERED - A subscription was recovered from account hold."; break;
                         case 2: str = "(2) SUBSCRIPTION_RENEWED - An active subscription was renewed."; break;
                         case 3: str = "(3) SUBSCRIPTION_CANCELED - A subscription was either voluntarily or involuntarily cancelled. For voluntary cancellation, sent when the user cancels."; break;
                         case 4: str = "(4) SUBSCRIPTION_PURCHASED - A new subscription was purchased."; break;
                         case 5: str = "(5) SUBSCRIPTION_ON_HOLD - A subscription has entered account hold (if enabled)."; break;
                         case 6: str = "(6) SUBSCRIPTION_IN_GRACE_PERIOD - A subscription has entered grace period (if enabled)."; break;
                         case 7: str = "(7) SUBSCRIPTION_RESTARTED - User has reactivated their subscription from Play > Account > Subscriptions (requires opt-in for subscription restoration)"; break;
                         case 8: str = "(8) SUBSCRIPTION_PRICE_CHANGE_CONFIRMED - A subscription price change has successfully been confirmed by the user."; break;
                         case 9: str = "(9) SUBSCRIPTION_DEFERRED - A subscription's recurrence time has been extended."; break;
                         case 10: str = "(10) SUBSCRIPTION_PAUSED - A subscription has been paused."; break;
                         case 11: str = "(11) SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED - A subscription pause schedule has been changed."; break;
                         case 12: str = "(12) SUBSCRIPTION_REVOKED - A subscription has been revoked from the user before the expiration time."; break;
                         case 13: str = "(13) SUBSCRIPTION_EXPIRED - A subscription has expired."; break;
                     }

                     switch(body.subscriptionNotification.notificationType) {
                        case 1:
                        case 2:
                        case 4:
                        case 6:
                        case 7:
                        case 8:
                        case 9: {
                           console.log("Falling in the boooya number, show me the money!!! -- " + body.subscriptionNotification.notificationType);
                           plan = body.subscriptionNotification.subscriptionId;
                           hasRemoteAccessEnabled = true;
                        } break;

                        case 11: {
                           console.log("Falling in the hummm number, subscription is paused :-( " + body.subscriptionNotification.notificationType);
                           plan = body.subscriptionNotification.subscriptionId;
                           hasRemoteAccessEnabled = true;
                        }
                        break;
                        
                        case 3:
                        case 5:
                        case 10:
                        case 12:
                        case 13:
                        default: {
                           console.log("Falling in the bahhhhhhhhhh case; No money -- " + body.subscriptionNotification.notificationType);
                           plan = "free";
                        } break;
                     }

                     // Now, update its subscription values.
                     return db.collection(COLLECTION_USER_EXTRA)
                        .doc(userId)
                        .set({
                                plan: plan,
                                planUpdateOrigin: "server",
                                hasRemoteAccessEnabled: hasRemoteAccessEnabled,
                                lastNotificationType: body.subscriptionNotification.notificationType
                             }, {merge: true})
                        .then(finished => {
                           console.log("PURCHASE_TRACE - user updated with plan=" + plan + ", and lastNotificationType=" + body.subscriptionNotification.notificationType);
                           sendEmail("sharedCounterNotif - Notif OK",
                                  "for plan= " + plan
                                    + "<br><br>userFound= true"
                                    + "<br><br>NotificationType= " + body.subscriptionNotification.notificationType
                                    + "<br><br>" + str
                                    + "<br><br>userId= " + userId
                                    + "<br><br>Token= " + purchaseToken


                                   );

                           return successCode;
                        });
                   } else {
                     // Could not find the user
                     console.log("PURCHASE_TRACE - user NOT found id=" + userId);
                     // return 404;
                     sendEmail("sharedCounterNotif - Notif KO",
                                  "for plan= " + plan
                                    + "<br><br>userFound=false"
                                    + "<br><br>NotificationType=" + body.subscriptionNotification.notificationType
                                    + "<br><br>" + str
                                    + "<br><br>Token= " + purchaseToken);

                     return errorCode;
                   }
                 });
            });
         });
   }

});


/*
ex:
{
   "version":"1.0",
   "packageName":"com.cdb.countpeople",
   "eventTimeMillis":"1591026462966",
   "subscriptionNotification":{
      "version":"1.0",
      "notificationType":4,
      "purchaseToken":"iabnemeolafkabjibhppgjfo.AO-J1OzGD-8MZSDlb3sgMceSRyKj9CQtKJ7EhoKOh0k_D2VRgzY75xd67oLVGVhdOME6D6mml7IMXsoQqmcQTIv-Ws3W9QwHGgkkXVRwgpJPLsRR63emu_AOo851Yde4XqPYuAQoHmxysPTfGlOtUgMdKATxhVGFzg",
      "subscriptionId":"subscription.full.monthly_10_eur"}
}

{
   "version":"1.0",
   "packageName":"com.cdb.countpeople",
   "eventTimeMillis":"1591026764820",
   "subscriptionNotification":{
      "version":"1.0",
      "notificationType":12,
      "purchaseToken":"iabnemeolafkabjibhppgjfo.AO-J1OzGD-8MZSDlb3sgMceSRyKj9CQtKJ7EhoKOh0k_D2VRgzY75xd67oLVGVhdOME6D6mml7IMXsoQqmcQTIv-Ws3W9QwHGgkkXVRwgpJPLsRR63emu_AOo851Yde4XqPYuAQoHmxysPTfGlOtUgMdKATxhVGFzg",
      "subscriptionId":"subscription.full.monthly_10_eur"}}
}

*/


function sendEmail(subject, message) {

   console.log("Will send email: subject=" + subject + ", message=" + message);

   return db.collection('mail')
      .add({
           to: ["info@shared-counter.fr"],
           message: {
              subject: subject,
              html: message
           }
      })
     .then(sent => {
        console.log('Queued email for delivery!');
        return 200;
      });
}

exports.onSchedulerCreated = functions.firestore.document(COLLECTION_SCHEDULERS + '/{schedulerId}').onCreate((snap, context) => {
   let schedulerId = context.params.schedulerId;

   // Need to triger the update, in order to set the offset elements.
   return db.collection(COLLECTION_SCHEDULERS).doc(schedulerId).set({uniqueOperation: metaActionUid()}, {merge: true});
});

exports.onSchedulerUpdated = functions.firestore.document(COLLECTION_SCHEDULERS + '/{schedulerId}').onUpdate((snap, context) => {
   let operationBefore = snap.before.data().uniqueOperation;
   let operationAfter = snap.after.data().uniqueOperation;

   if (operationBefore === operationAfter) {
      console.log("Scheduler onUpdate: this is not a user trigered update, skipping");
      return null;
   }

   let timeZoneData = {
      uniqueOperation: operationAfter
   };

   let normalizedHours = [];

   if (snap.after.data().active) {
      let hourToConsider = snap.after.data().shootAtDisplay;
      let gmtOffsetInHours = snap.after.data().gmtOffsetInHours;

/*      for (var i = 0; i < 7; i++) {
          if (snap.after.data().wds[i] === true) {
             normalizedHours.push(convertToWeekHours(hourToConsider, i, gmtOffsetInHours));
          }
      }
*/

      if (snap.after.data().wd0 === true) {
         console.log("wd0 is active");
         normalizedHours.push(convertToWeekHours(hourToConsider, 0, gmtOffsetInHours));
      }
      if (snap.after.data().wd1 === true) {
         console.log("wd1 is active");
         normalizedHours.push(convertToWeekHours(hourToConsider, 1, gmtOffsetInHours));
      }
      if (snap.after.data().wd2 === true) {
         console.log("wd2 is active");
         normalizedHours.push(convertToWeekHours(hourToConsider, 2, gmtOffsetInHours));
      }
      if (snap.after.data().wd3 === true) {
         console.log("wd3 is active");
         normalizedHours.push(convertToWeekHours(hourToConsider, 3, gmtOffsetInHours));
      }
      if (snap.after.data().wd4 === true) {
         console.log("wd4 is active");
         normalizedHours.push(convertToWeekHours(hourToConsider, 4, gmtOffsetInHours));
      }
      if (snap.after.data().wd5 === true) {
         console.log("wd5 is active");
         normalizedHours.push(convertToWeekHours(hourToConsider, 5, gmtOffsetInHours));
      }
      if (snap.after.data().wd6 === true) {
         console.log("wd6 is active");
         normalizedHours.push(convertToWeekHours(hourToConsider, 6, gmtOffsetInHours));
      }
   }

   timeZoneData.hoursOfWeek = normalizedHours;

   // Now, need to update the TZ stuff
   return db.collection(COLLECTION_SCHEDULERS).doc(context.params.schedulerId).set(timeZoneData, {merge: true});
});

function convertToWeekHours(localHour, day, offset) {
   let x = (day * 24) + localHour - offset;

   if (x >= 24 * 7) {
      return x  - (24 * 7);
   } else if (x < 0) {
      return x + (24 * 7);
   }

   return x;
}


exports.onPurchaseHistoryTempCreated = functions.firestore.document(COLLECTION_PURCHASES_HISTORY_TEMP + '/{purchaseHistoryTempEntry}').onCreate((snap, context) => {
   // Ok, we have a temp new entry here. Check if we have the consolidated list available. If so, add to it + remove, else, wait.
   if (pushMode === PUSH_MODE_NEW) {

      let purchaseToken = snap.data()['purchaseToken'];
      let date = snap.data()['date'];
      let body = snap.data()['body'];
      let purchaseHistoryTempEntryId = context.params.purchaseHistoryTempEntry;
      let userId;
      
      console.log("Has created purchaseHistoryTemp:" + purchaseHistoryTempEntryId);

      return db.collection(COLLECTION_PURCHASES_HISTORY)
         .where("purchaseToken", "==", purchaseToken)
         .limit(1)   // Should never be more
         .get()
         .then(found => {
            if (found.empty) {
               console.log("purchaseHistory has no entry with purchaseToken=" + purchaseToken);
               return false;
            } else {
               // There is one. Let's concatenate our current record, then update, then remove
               console.log("There is already a purchaseHistory document with this token");
               let purchaseEntryRef = found.docs[0].ref;
               let purchaseEntry = found.docs[0].data();
               let actions = purchaseEntry.actions || [];
               userId = purchaseEntry.userId;

               // Add the new one.
               actions.push({
                  notificationType: body.subscriptionNotification.notificationType,
                  date: date,
                  packageName: body.packageName
               });

               let toUpdate = {
                  actions: actions,
                  lastNotificationType: body.subscriptionNotification.notificationType
               };

               return purchaseEntryRef.set(toUpdate, {merge: true})
                  .then(updated => {
                     console.log("purchaseHistory updated with the temp entry that was found before. temp entry can be deleted now:" + purchaseHistoryTempEntryId);
                     return db.collection(COLLECTION_PURCHASES_HISTORY_TEMP).doc(purchaseHistoryTempEntryId).delete();
                     // return true;
                  })
                  .then(x => {
                     return true;
                  });
            }
         })
         ;
      } else {
         return false;
      }
   });

/*
 * This method automatically adds server timestamp to the record. Can't be hacked by a device that has the wrong time.
 */
exports.onPurchaseHistoryCreation = functions.firestore.document(COLLECTION_PURCHASES_HISTORY + '/{purchaseEntry}').onCreate((snap, context) => {

   let purchaseToken = snap.data()['purchaseToken'];

   if (purchaseToken === undefined) {
      console.log("purchaseToken not known yet - this is surely a creation coming from the mobile. No need to go any further; Will come later soon");
      return 0;
   }

   let purchaseEntryId = context.params.purchaseEntry;

   // console.log("in purchaseEntry created-1:" + snap.data());
   console.log("in purchaseEntry created-3:" + JSON.stringify(snap.data()));

   console.log("in purchaseEntry created: " + purchaseEntryId + ", purchaseToken=" + purchaseToken);

   var now = new Date();
   var extra = {
      creationDate: now,
      actions: []
   };

   let purchaseHistoryRef = db.collection(COLLECTION_PURCHASES_HISTORY).doc(purchaseEntryId);

   if (pushMode === PUSH_MODE_NEW) {
      // First, let's add the data and table.
     return purchaseHistoryRef.set(extra, {merge: true})
        .then(initialized => {
            console.log("purchase entry document created");
            return 0;
       })
       .then(merged => {
         console.log("Looking for temp purchase entries");
         return db.collection(COLLECTION_PURCHASES_HISTORY_TEMP)
            .where("purchaseToken", "==", purchaseToken)
            .orderBy("date")
            .get();
       })
       .then(matches => {
          if (matches.empty) {
             console.log("No pending actions in COLLECTION_PURCHASES_HISTORY_TEMP, returning");
             return 0;
          }

          // Because it's a creation, the list of actions is necessarily empty.
          let actions = [];

          // And there is no last notification
          let lastNotificationType;

          matches.forEach(match => {
             let temp = match.data();

             actions.push({
                notificationType: temp.body.subscriptionNotification.notificationType,
                packageName: temp.body.packageName,
                date: temp.date
            });

            lastNotificationType = temp.body.subscriptionNotification.notificationType;

            console.log("Could now delete this entry, usefull data retrieved and ready to be added to COLLECTION_PURCHASES_HISTORY");
          });

          console.log("Will now update the document in COLLECTION_PURCHASES_HISTORY");
          let toUpdate = {
                            actions: actions,
                            lastNotificationType:lastNotificationType
                         };

          console.log("purchaseHistory entry to update=" + purchaseEntryId + ", will update it with:" + JSON.stringify(toUpdate));

          return purchaseHistoryRef
                   .set(toUpdate, {merge: true});
       })
       .then(mergedWithTemp => {
          console.log("All done!!");
         return 0;
       });
   }
   else
   {
      return purchaseHistoryRef.set(extra, {merge: true})
        //.then(doc => {
        //   // All good.
        //   console.log(COLLECTION_PURCHASES_HISTORY + " updated with time - now checking if we missed the first bits");
        //   return 0;
        //})
        .then(updated => {
           console.log("Now checking if we have pending elements in COLLECTION_PURCHASES_HISTORY_TEMP with token=");
           // Check if we have the same token in TEMP
           return db.collection(COLLECTION_PURCHASES_HISTORY_TEMP)
             .where("purchaseToken", "==", purchaseToken)
             .get()
             .limit(1)
             .then(matches => {
               // We should find 0 or 1 only
               if (matches.empty) {
                  console.log("There was no entry in the temp having this token. Finished");
                  return 0;
               } else {
                  console.log("Found a pending element in COLLECTION_PURCHASES_HISTORY_TEMP having this token");
                  let actions = purchaseHistoryRef.data()['actions'] || [];
                  let lastNotificationType = purchaseHistoryRef.data()['lastNotificationType'];

                  matches.forEach(purchaseHistoryTemp => {
                    let body = purchaseHistoryTemp.data()['body'];

                    console.log("Body is: " + body.toJSON());

                    // Reuse the date fro the temp entry
                    let date = new Date(Number(body.eventTimeMillis));

                    // Build the element to insert at the top of the array (position 0)
                    let entryToInsert = {
                      notificationType: body.subscriptionNotification.notificationType,
                      packageName: body.packageName,
                      date: date
                    };

                    // Insert it
                    actions.splice(0, 0, entryToInsert);

                    // This is the notification type we want to use for update the USER_EXTRA. So, updating the hsitory with this one.
                    lastNotificationType = body.subscriptionNotification.notificationType;

                    console.log("Pending entry used. Can now be safely deleted.");

                  });

                  return purchaseHistoryRef.set({actions: actions, lastNotificationType: lastNotificationType}, {merge: true})
                    .then(fixed => {
                           console.log("History successfully updated with pending entrie.");
                           return 0;
                        });
               }
             });
        });
   }
});

function catchupPurchaseHistoryTemp(purchaseEntryId, purchaseToken, purchaseHistoryData) {

   return db.collection(COLLECTION_PURCHASES_HISTORY_TEMP)
      .where("purchaseToken", "==", purchaseToken)
      .orderBy("date")
      .get()
      .then(matches => {
          if (matches.empty) {
            console.log("Nothing to catchup for COLLECTION_PURCHASES_HISTORY, returning");
            return 0;
         }

         // This insures we add to the existing array of actions, or create a new one if does not exist yet.
         let actions = purchaseHistoryData.actions || [];

         // And there is no last notification
         let lastNotificationType;

         matches.forEach(match => {
            let temp = match.data();

            actions.push({
               notificationType: temp.body.subscriptionNotification.notificationType,
               packageName: temp.body.packageName,
               date: temp.date
            });

            lastNotificationType = temp.body.subscriptionNotification.notificationType;

            console.log("Could now delete this entry, usefull data retrieved");
            match.ref.delete();
         });

         console.log("Will now update the document in COLLECTION_PURCHASES_HISTORY");
         let toUpdate = { actions: actions, lastNotificationType:lastNotificationType };

         console.log("purchaseHistory entry to update=" + purchaseEntryId + ", will update it with:" + JSON.stringify(toUpdate));

         return db.collection(COLLECTION_PURCHASES_HISTORY)
            .doc(purchaseEntryId)
            .set(toUpdate, {merge: true});
      })
      .then(mergedWithTemp => {
         console.log("PurchaseHistory catchup success");
         return 0;
      });
}

exports.onPurchaseHistoryUpdated = functions.firestore.document(COLLECTION_PURCHASES_HISTORY + '/{purchaseHistory}').onUpdate((snap, context) => {
   let userId = snap.after.data().userId;
   let lastNotificationType = snap.after.data().lastNotificationType;
   let purchaseToken = snap.after.data().purchaseToken;
   let sku = snap.after.data().sku;

   // If this is undefined, it means we never had a chance to update this document from history temp elements. It means the update is coming from the mobile
   // right after a purchase. There are chances Google already pushed some billing status even before we reach this point. Therefore, taking a look into the
   // elements that may already have this purchase token.
   if (lastNotificationType === undefined) {
      console.log("No last notificiation known yet - user can't be updated. Maybe pending historyPurchaseTemp items assocoated to this purchaseToken? Checking now.");

     //Look for temp entries, and update if found. Updating it will call this again, and this time, lastNotificationType will be !== undefined
     let purchaseHistoryId = context.params.purchaseHistory
     return catchupPurchaseHistoryTemp(purchaseHistoryId, purchaseToken, snap.after.data());
   }

   console.log("COLLECTION_PURCHASES_HISTORY was updated. Let's have it reflected on the user extra:" + userId);

   let str = "";
   switch(lastNotificationType) {
      case 1: str = "(1) SUBSCRIPTION_RECOVERED - A subscription was recovered from account hold."; break;
      case 2: str = "(2) SUBSCRIPTION_RENEWED - An active subscription was renewed."; break;
      case 3: str = "(3) SUBSCRIPTION_CANCELED - A subscription was either voluntarily or involuntarily cancelled. For voluntary cancellation, sent when the user cancels."; break;
      case 4: str = "(4) SUBSCRIPTION_PURCHASED - A new subscription was purchased."; break;
      case 5: str = "(5) SUBSCRIPTION_ON_HOLD - A subscription has entered account hold (if enabled)."; break;
      case 6: str = "(6) SUBSCRIPTION_IN_GRACE_PERIOD - A subscription has entered grace period (if enabled)."; break;
      case 7: str = "(7) SUBSCRIPTION_RESTARTED - User has reactivated their subscription from Play > Account > Subscriptions (requires opt-in for subscription restoration)"; break;
      case 8: str = "(8) SUBSCRIPTION_PRICE_CHANGE_CONFIRMED - A subscription price change has successfully been confirmed by the user."; break;
      case 9: str = "(9) SUBSCRIPTION_DEFERRED - A subscription's recurrence time has been extended."; break;
      case 10: str = "(10) SUBSCRIPTION_PAUSED - A subscription has been paused."; break;
      case 11: str = "(11) SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED - A subscription pause schedule has been changed."; break;
      case 12: str = "(12) SUBSCRIPTION_REVOKED - A subscription has been revoked from the user before the expiration time."; break;
      case 13: str = "(13) SUBSCRIPTION_EXPIRED - A subscription has expired."; break;
   }

   switch(lastNotificationType) {
      case 1:
      case 2:
      case 4:
      case 6:
      case 7:
      case 8:
      case 9: {
         console.log("Falling in the boooya number, show me the money!!! -- " + lastNotificationType);
         plan = sku;
      } break;

      case 11: {
         console.log("Subscription paused :-( " + lastNotificationType);
         plan = sku;
      } break;
      
      case 3:
      case 5:
      case 10:
      case 12:
      case 13:
      default: {
         console.log("Falling in the bahhhhhhhhhh case; No money -- " + lastNotificationType);
         plan = "free";
      } break;
   }

   let updates = {
      lastNotificationType: lastNotificationType,
      planUpdateOrigin: "server",
      plan: plan
   };

   // Look for the user who is referenced in the updated document.
   return db.collection(COLLECTION_USER_EXTRA)
      .doc(userId)
      .set(updates, {merge: true})
      .then(updated => {
         sendEmail("sharedCounterNotif - User plan updated",
               "for plan= " + plan
                + "<br><br>NotificationType=" + lastNotificationType
                + "<br><br>" + str
                + "<br><br>userId= " + userId
                + "<br><br>Token=" + purchaseToken);

        return 0;
      })
      .then(x => {
         if (SEND_WELCOME_EMAIL === false) {
            console.log("Welcome email not supported yet");
            return null;
         }
         else
         if (lastNotificationType === 4) {
            // This is a new purchase, we want to get the user details to send a welcome email
            return getDetailsForUserId(userId);
         }
         else {
            return null;
         }
      })
      .then(user => {
         if (user !== null && user.email !== undefined) {
            // Should shoot the welcome paid email
            let templateName = `welcome_subscription_${user.language}`;
               
            let template = {
               name: templateName,
               data: {
                  displayName: user.displayName || ""
               }
            };
         
            return sendEmailTo(user, template, true);
         }
         else {
            console.log("Not a new subscription, skipping welcome email");
            return null;
         }
      })
      ;
      
   });

/*
exports.joinEvent = functions.https.onCall((data, context) => {

   let docRef = db.collection('events').doc(data.eventId);
   let callerId = data.userId;
   let eventId = data.eventId;

   return docRef
      .get()
      .then(doc => {
         if (!doc.exists) {
            console.log('No such document!');
            return {
               success: false,
               error: "document not found"
            };
         } else {
            console.log('Document data:', doc.data());
            var newData = doc.data();

            //console.log('newData data:', newData);
            //console.log('newData data["contributors"]:'+ newData['contributors']);

            if (newData['contributors'].indexOf(callerId) < 0) {
               newData['contributors'].push(callerId);
            }

            return docRef
               .set(newData)
               .then(() => {
                     console.log('Document data after join');
                     return {
                        success: true,
                        eventId: eventId
                     };
                  }
               );
         }
      })
      .catch(err => {
         console.log('Error getting document', err);
      });
});
*/

exports.createEvent = functions.https.onCall((data, context) => {
   let userId = data.userId;
   let userRef = db.collection(COLLECTION_USER_EXTRA).doc(userId);
   let maxValue = data.maxValue;
   let title = data.title;
   let security = data.security;

   return checkAndCreateEvent(userId,
      userRef,
      maxValue,
      title,
      security,
      null,
      null,
      0, // default group
      "user");
});

exports.raiseHitsQuotaRequest = functions.https.onCall((data, context) => {
   // 1- Get the counterOnly record for whish the quota raise is requested
   // 2- if found, check if the request was issued by the owner of the counter.
   // 3- If so, get the owner's details, including currentPlan
   // 4- If found, get the purchasable entry matching the user's plan
   // 5- If found, get the maxHit value associated to this plan
   // 6- Then, finally update the counterOnly record, with the max hits for the user's plan.
   
   let userId = data.userId;
   let eventId = data.eventId;
   let response = { success: false};
   let maxHitsValue;
   let counterOnlyRef = db.collection(COLLECTION_COUNTER_ONLY).doc(eventId);
   let userPlan = null;
   
   console.log(`Handling quota raise for user=${userId} on eventId=${eventId}`);
   
/*   console.log("raise request for user:" + userId);
   response.newMaxHits = 200000;
   response.success = true;
   
   return response;
*/   
   
   
   return counterOnlyRef
      .get()
      .then(co => {
         if (co.exists) {
            if (co.data().owner === userId) {
               console.log("counterOnly found, and user is the owner, great.");
               return co;
            } else {
               console.log("counterOnly found, by requestor is NOT the owner");
               return null;
            }
         } else {
            console.log("counterOnly NOT found");
            return null;
         }
      })
      .then(co => {
         if (co !== null) {
            return db.collection(COLLECTION_USER_EXTRA)
               .doc(userId)
               .get();
         } else {
            console.log("Counter only was not found, or user is not the owner. Returning null");
            return null;
         }
      })
      .then(user => {
         if (user !== null && user.exists) {
            console.log("user found");
            userPlan = user.data().plan;
            return userPlan;
         } else {
            console.log("returning null because of previous reasons, or user not found");
            return null;
         }
      })
      .then(plan => { 
         if (plan !== null) {
            return db.collection(COLLECTION_PURCHASABLE_PRODUCTS)
               .doc(plan)
               .get();
         } else {
            console.log("returning null because of previous reasons, or plan is null");
            return null;
         }
      })
      .then(purchasableEntry => {
         if (purchasableEntry !== null && purchasableEntry.exists) {
            maxHitsValue = purchasableEntry.data().maxHits;
            console.log("maxHits found for this request, and is:" + maxHitsValue);
            return maxHitsValue;
         } else {
            console.log("returning null because of previous reasons, or purchasable product not found for given plan");
            return null;
         }
      })
      .then(maxHits => {
         if (maxHits !== null) {
            console.log("We have the max hits != null, it means ALL above passed ok. Updating the maxHits and ownPlan as well");
            return counterOnlyRef
               .set({maxHits: maxHitsValue, ownerPlan: userPlan}, {merge: true});
         } else {
            console.log("returning null because of previous reasons, or okan's maxHit is null");
            return null;
         }
      })
      .then(also1 => {
         if (also1 !== null) {
            // Upgrade the metadata as well
            return db.collection(COLLECTION_EVENT_META_DATA)
               .doc(eventId)
               .set({ownerPlan: userPlan}, {merge: true});
         } else {
            return null;
         }
      })
      .then(updated => {
         if (updated !== null) {
            console.log("CounterOnly was updated");
            response.success = true;
            response.newMaxHits = maxHitsValue;
            return response;
         } else {
            console.log("returning null because of previous reasons");
            response.success = true;
            return response;
         }
      })
      ;
});

// This method can be used when user creates an event, or when it is triggered by a scheduler.
function checkAndCreateEvent(userId,userRef, maxValue, title, security, remoteAccessId, gmtOffset, group, createdBy) {
   return userRef
      .get()
      .then(user => {
         console.log("found user, now checking if event can be created. UserId=" + userId);
         if (!user.exists) {
            console.log("user not found. Aborting event creation.");
            return { success: false, reason: "user_not_found" };
         }

         let vip = user.data().isVip || false;
         if (vip) {
            console.log("User is VIP createEvent will succeed like for paid:" + userId);
         }

         // Check how many active events for this user, ie that expire after now.
         let now = new Date();
         return db.collection(COLLECTION_EVENT_META_DATA)
            .where("expiresOn", ">", now)
            .where("owner", "==", userId)
            .get()
            .then(matches => {

               let activeCounters = [];
               let nonExpired = 0;
               matches.forEach(match => {
                  console.log(`non expired event: ${match.data().title}, id=${match.data().eventId}`);
                  activeCounters.push(match.data().eventId);
                  nonExpired++;
               });

               if (user.data()["plan"] === undefined) { 
                  console.log("user plan is undefined!!! - STRANGE- ROLLING BACK TO 'free'");
                  
                  // We have to set it here too, because the 'user' is used inside the doCreateEvent
                  user.data()["plan"] = "free";
               }
               
               let userPlan = user.data()["plan"] || "free";
               console.log("has nonExpired:" + nonExpired + ", and plan=" + userPlan);
               
               if (vip) {
                  console.log("User is VIP, forcing paid plan for checks.");
                  userPlan = "subscription.full.monthly_10_eur";
               }
               
               let maxCounters = concurrentCounters[userPlan];
               console.log("max allowed counters=" + maxCounters);
               
               // If there is no live counters, we can always create.
               if (nonExpired === 0) {
                  console.log("This user has no active events. Allowed to create a new one");
                  return doCreateEvent(user.data(), title, maxValue, security, createdBy, remoteAccessId, gmtOffset, group);
               } else if ((userPlan !== undefined && userPlan.indexOf("subscription") >= 0) || vip) {
                  // Paid plan.
                  if (nonExpired < maxCounters) {
                     console.log("User has a subscription, but less than " + maxCounters + " active counter. Allowed to create a new one");
                     return doCreateEvent(user.data(), title, maxValue, security, createdBy, remoteAccessId, gmtOffset, group);
                  } else {
                     console.log("User has paid plan, but found " + nonExpired + " active events");
                     return { success: false, reason: "number_of_concurrent_events_quota_exceeded_paid", extra: "2", activeCounters: activeCounters};
                  }
               } else {
                  console.log("User has free plan, but found " + nonExpired + " active events");
                  return { success: false, reason: "number_of_concurrent_events_quota_exceeded_free", extra: "1", activeCounters: activeCounters};
               }
            });
      })
      .catch(err => {
         console.log("Issue creating event:", err);
         return {success: false, reason: "event_error" };
      });
}




function doCreateEvent(user, title, maxValue, security, createdBy, remoteAccessId, gmtOffset, group) {

   let newEventRef = db.collection(COLLECTION_EVENT_META_DATA);
   let now = new Date();
   let contributors = [];
   
   contributors.push(user.userId);

   let eventData = {
      owner: user.userId,
      ownerPlan: user.plan,
      counter: 0,
      maxValue: maxValue,
      contributors: contributors,
      createdBy: createdBy,
      group: group
   };

   if (title !== undefined) {
      eventData.title = title;
   }

   return db.collection(COLLECTION_PURCHASABLE_PRODUCTS)
      .doc(user.plan)
      .get()
      .then(plan => {
         let maxHits;
         if (plan.exists) {
            maxHits = plan.data().maxHits;
            console.log(`Max hits for user's plan is ${maxHits}`);
         } else {
            maxHits = 1000;
            console.log(`Could not get the user plan, using ${maxHits} instead`);
         }
         return maxHits;
      })
      .then(maxHits => {
         return newEventRef
            .add(eventData)
            .then(ref => {
               console.log("MetaData doc now created ok:" + ref.id);
               // Creating the security record, to avoid multiple user with same account
               createSecurityEntry(ref, security);

               let counterOnly = {
                  counter: 0, 
                  nbChunks: NB_CHUNKS,
                  abs: 0,
                  hits: 0,
                  maxHits: maxHits,
                  owner: user.userId,
                  ownerPlan: user.plan,
                  createdAt: new Date()
               };

               if (remoteAccessId !== undefined && remoteAccessId !== null) {

                  // This user has been disallowed to use remote access. Remove the associated remote access document. The counterOnly to be created
                  // will not contain the remote access doc name, therefore, won't be updated when counterOnly changes.            
                  if (user.raiForbidden === true) {
                     // In this case, delete the record.
                     db.collection(COLLECTION_REMOTE_ACCESS)
                        .doc(user.userId + "-" + remoteAccessId)
                        .delete();
                  }
                  else {
                     counterOnly.rai = user.userId + "-" + remoteAccessId;

                     // And take this opportunity to create the remote access record. If it exists, will just be overriden. We don't have to wait until it is done, because not 
                     // vital for the rest of the operations.
                     let remoteAccessData = {ownerId: user.userId, rai: remoteAccessId, counter: 0};
                     if (gmtOffset !== undefined) {
                        remoteAccessData.gmtOffset = gmtOffset;
                     }
                     db.collection(COLLECTION_REMOTE_ACCESS)
                        .doc(user.userId + "-" + remoteAccessId)
                        .set(remoteAccessData, {merge: true});
                  }
               }

               console.log("Will now create the metadata.");

               // Need to create the matching counterOnly document with the same meta data id, otherwise client may think the event is not valid.
               return db.collection(COLLECTION_COUNTER_ONLY)
                  .doc(ref.id)
                  .set(counterOnly)
                  .then(refCounterOnly => {
                     console.log("counterOnly doc now created ok");

                     return {
                        success: true,
                        eventId: ref.id
                     };
                  });
            });   
      });
}

function createSecurityEntry(eventRef, security) {

   let securities = [];

   if (security !== undefined) {
      // Build the first security record for this event.
      console.log("Will create security records");
      let userSecurityData = JSON.parse(security);
      securities.push(userSecurityData);
   } else {
      // Device didn't provide security information (old version).
      console.log("No security control provided, security record will be empty");
   }

   let securityData = {
      eventId: eventRef.id,
      userSecurityChecks: securities
   };

   db.collection(COLLECTION_SECURITY).doc(eventRef.id).set(securityData, { merge: true});
}

function metaActionUid() {
   return Math.floor(Math.random() * Number.MAX_SAFE_INTEGER);
}

exports.resetEvent2 = functions.https.onCall((data, context) => {
   let metaDataRef = db.collection(COLLECTION_EVENT_META_DATA).doc(data.eventId);

   return metaDataRef
      .get()
      .then(metaData => {

         var nbChunks = metaData.data().nbChunks;

         let metaAction = metaActionUid();
         console.log("MetaData retreived ok for reset - metaAction = " + metaAction);

         return metaDataRef.set({
                        counter: 0,
                        maxCounter:0,
                        maxCounterTimeStamp: null,
                        metaActionId: metaAction}, {merge: true})
            .then(() => {

               console.log("In once metaData has been updated");

               // Also, reset each single chunk
               var resets = [];

               for (var i = 0; i< nbChunks; i++) {
                  var chunkId = data.eventId + "-" + i;

                  console.log("Will reset chunk " + chunkId);
                  resets.push(db.collection(COLLECTION_HISTORY_CHUNKS)
                     .doc(chunkId)
                     .set({inOuts:[], timeStamp:null}, {merge:true}));
               }

               return Promise.all(resets);
            })
            .then(msg => {
               return db.collection(COLLECTION_MESSAGES)
                  .doc(data.eventId)
                  .set({ });
            })
            .then(ctr => {
               return db.collection(COLLECTION_COUNTER_ONLY)
                  .doc(data.eventId)
                  .set({
                     counter: 0,
                     maxCounter:0,
                     maxCounterTimeStamp: null,
                     abs: 0}, {merge: true});
            })
            .then(() => {
               return { success: true };
            });
         })
         .catch(err => {
            console.log('Error getting document', err);
         });
   });

/*
exports.joinEvent3 = functions.https.onCall((data, context) => {

   let docRef = db.collection(COLLECTION_EVENT_META_DATA).doc(data.eventId);
   let callerId = data.userId;
   let eventId = data.eventId;

   console.log("in joinEvent3, eventId=" + eventId + ", callerId=" + callerId);

   return docRef
      .get()
      .then(doc => {
         if (!doc.exists) {
            console.log('No such document! Event does not exist, so cant join it');
            return {
               success: false,
            };
         } else {
            // console.log('Document data:', doc.data());
            var newData = doc.data();
            var contributors = newData.contributors;

            //console.log('newData data:', newData);
            console.log('contributors before joint3 is ' + contributors);

            // Add this user amongst the contributors, if not already in it.
            if (contributors.indexOf(callerId) >= 0) {
                // Already in there, just returning the list of contributors.
                return {
                   success: true,
                   doc: newData
                };
            }

            contributors.push(callerId);
            newData.contributors = contributors;

            return docRef
               .set({contributors: contributors}, {merge: true})
               .then(() => {
                     // console.log('Document data after join');
                     return {
                        success: true,
                        doc: newData
                     };
                  }
               );
         }
      })
      .catch(err => {
         console.log('Error getting document', err);

      });
});
*/

exports.createUserExtras = functions.auth.user().onCreate(user => {

   var now = new Date();

   var empty = {
      userId: user.uid,
      createdAt: now,
      plan: "free",
      planUpdateOrigin: "server"
   };

   return (db.collection(COLLECTION_USER_EXTRA)
      .doc(user.uid)
      .set(empty))
      .then(doc => {
         return saveUser(user);
      });
});

exports.userHouseKeeping = functions.auth.user().onDelete(user => {
   console.log("Delete extras for user:" + user.uid);
   
   return db.collection(COLLECTION_USER_EXTRA)
      .doc(user.uid)
      .delete()
      .then(deleted => {
         // Also, looking for all sheduler for this user.
         return db.collection(COLLECTION_SCHEDULERS)
            .where("userId", "==", user.uid)
            .get()
            .then(schedulers => {
               if (!schedulers.empty) {
                  let toDelete = [];
            
                  schedulers.forEach(scheduler => {
                     console.log("Will remove scheduler for user being deleted:" + user.uid);
                     toDelete.push(scheduler.ref.delete());
                  });
                  return Promise.all(toDelete);
               } else {
                  return Promise.resolve(true);
               }
            })
            .then(x => {
               // Now, delete the counters created by this person. Just delete the metadata owner by this person, the associated documents will be cascaded.
               return db.collection(COLLECTION_EVENT_META_DATA)
                  .where("owner", "==", user.uid)
                  .get();
            })
            .then(allToDelete => {
               if (!allToDelete.empty) {
                  let metaDataToDelete = [];
               
                  allToDelete.forEach(md => {
                     metaDataToDelete.push(md.ref.delete());
                  });
               
                  return Promise.all(metaDataToDelete);
               } else {
                  return Promise.resolve(true);
               }
            })
      })
});

exports.onDeleteEvent = functions.firestore.document(COLLECTION_EVENT_META_DATA + '/{eventId}').onDelete((snap, context) => {
   let eventId = context.params.eventId;
   let nbChunks = snap.data().nbChunks === undefined ? NB_CHUNKS : snap.data().nbChunks;

   let toBeDeleted = [];
   var i;
   for (i = 0; i < nbChunks; i++) {
      let chunkId = eventId + "-" + i;
     // console.log("Will delete chunk: " + chunkId);
      toBeDeleted.push(db.collection(COLLECTION_HISTORY_CHUNKS).doc(chunkId).delete());
   }

   // The meta data is deleted, obviously. Now need to clean history, messages, and counterOnly documents as well.
   toBeDeleted.push(db.collection(COLLECTION_MESSAGES).doc(eventId).delete());
   toBeDeleted.push(db.collection(COLLECTION_COUNTER_ONLY).doc(eventId).delete());
   toBeDeleted.push(db.collection(COLLECTION_SECURITY).doc(eventId).delete());
   
   
   return Promise.all(toBeDeleted);
});

/*
function createBuffer(user) {
   return db.collection(COLLECTION_USER_BUFFERS)
      .doc(user.uid)
      .set({ });
}
*/

function saveUser(user) {
   console.log(user);

   admin.auth()
     .getUser(user.uid)
     .then(userRecord => {
         // See the UserRecord reference doc for the contents of userRecord.
         console.log('Successfully fetched user data:' + userRecord.toJSON());

         var email = userRecord.email || null;
         var displayName = userRecord.displayName || "???";
         var photoURL = userRecord.photoURL || null;

         return (db.collection(COLLECTION_USER_EXTRA)
           .doc(userRecord.uid)
           .set({ email, displayName, photoURL}, {merge: true}))
           .then(writeResult => {
              console.log("user created");
              return;
           })
           .catch(err => {
              console.log(err);
              return;
           });
      })
     .catch(error => {
        console.log('Error fetching user data:', error);
     });
}




/**
 * This automatically creates the messages associated to the event id.
 */
exports.onEventCreation = functions.firestore.document(COLLECTION_EVENT_META_DATA + '/{eventId}').onCreate((snap, context) => {
   console.log("eventId: ", context.params.eventId);

   let contributors = snap.data()['contributors'];
   console.log("contributors at event creation: ", contributors);

   let createdAt = new Date();
   let expiresOn = new Date();
   expiresOn.setHours(expiresOn.getHours() + 20);
   expiresOn.setMilliseconds(0);
   expiresOn.setSeconds(0);

   let title = snap.data()['title'];
   if (title === undefined) {
      console.log("Has no title provided, will generate one");
      title = createdAt.toISOString()
         .replace(/T/, ' ')
         .replace(/\..+/, '');
   }

   return db.doc(COLLECTION_EVENT_META_DATA + '/' + context.params.eventId)
      .set({ createdAt: createdAt,
             expiresOn: expiresOn,
             title: title,
             eventId: context.params.eventId,
             nbChunks: NB_CHUNKS},
           { merge:true})
      .then( x => {
          return db.doc('messages/' + context.params.eventId).set({ });
      })
      .then(() => {
         var chunkIds = [];

         var i;
         for (i = 0; i < NB_CHUNKS; i++) {
            let chunkId = context.params.eventId + "-" + i;

            console.log("Adding chunck id in table: " + chunkId);
            chunkIds.push(db.doc(COLLECTION_HISTORY_CHUNKS + '/' + chunkId).set( { id: context.params.eventId, inOuts: [] } ));
         }

         return Promise.all(chunkIds);
      });
  });


exports.onCounterOnlyUpdated = functions.firestore.document(COLLECTION_COUNTER_ONLY + '/{eventId}').onUpdate((snap, context) => {
   let counterDataBefore = snap.before.data();
   let counterDataAfter = snap.after.data();
   let ctrBefore = counterDataBefore.counter || 0;
   let ctrAfter = counterDataAfter.counter || 0;
   let userId = counterDataAfter.userId;
   let delta = ctrAfter - ctrBefore;
   let eventId = context.params.eventId;
   let rai = snap.after.data().rai;
   
   if (delta === 0) {
      console.log("delta == 0, no need to update history and all. Saves one (or 2 if 'rai' enabled) useless WR action");
      return 0;
   }
   
   // console.log(`onCounterOnlyUpdated update. (before, after)=(${ctrBefore}, ${ctrAfter}) for eventId = ${eventId}, rai = ${rai}`);
   let now = new Date();

   return updateHistory(eventId, delta, ctrAfter, counterDataAfter, now, userId)
      .then(x => {
         // If there is a rai, it means at creation time, the user was allowed to do it.
         if (rai !== undefined) {
            // Need to update the remote Access id record as well.
            return db.collection(COLLECTION_REMOTE_ACCESS)
              .doc(rai)
              .set({counter: ctrAfter, ts: now}, {merge: true});
         } else {
            return Promise.resolve(true);
         }
      });
});


exports.onMetaDataUpdated = functions.firestore.document(COLLECTION_EVENT_META_DATA + '/{eventId}').onUpdate((snap, context) => {
   let eventDataBefore = snap.before.data();
   let eventDataAfter = snap.after.data();
   let eventId = context.params.eventId;
   let contributorsBefore = eventDataBefore.contributors || [];
   let contributorsAfter = eventDataAfter.contributors || [];
   
   // If the event meta data change is a person leaving the event, we have to insure the same security elements are gone, otherwise user won't be able to join again.
   if (contributorsBefore.length > contributorsAfter.length) {
      console.log("One contributor has left. Update the security as well");

      // Find the contributor(s) who left the event.
      let removedContributors = contributorsBefore.filter( x => contributorsAfter.indexOf(x) < 0);

      console.log("contributors who left: " + removedContributors);

      // This will delete the event + cascade all subsequent deletions.
      return db.collection(COLLECTION_SECURITY).doc(eventId)
         .get()
         .then(security => {
            if (!security.exists) {
               console.log("There is no security collection for this event. Suspicious 1");
               return null;
            }
            else {
               // Remove the entry for this user id.
               let securityData = security.data();
               let list = securityData.userSecurityChecks;

               if (list !== undefined) {
                  let remainingSecurityEntries = list.filter(x => removedContributors.indexOf(x.userId) < 0);
                  console.log("In security, we should now update the list to: " + JSON.stringify(remainingSecurityEntries));

                  return remainingSecurityEntries;
               } else {
                  console.log("Can't find the security list for this event. Suspicisous 2");
                  return null;
               }
            }
         })
         .then(newList => {
            if (newList !== null) {
               console.log("Will update the security entries with list=" + JSON.stringify(newList));
               return db.collection(COLLECTION_SECURITY)
                  .doc(eventId)
                  .set( {userSecurityChecks: newList}, {merge: true});
            } else {
               console.log("No security entry to update - suspicious");
               return 0;
            }
         });
   }

   return 0;
});


// Can be called on event update (legacy), and counterValueOnly (optimized)
function updateHistory(eventId, delta, absolute, docData, now, userId) {
   let nbChunksForEvent = (docData.nbChunks === undefined) ? NB_CHUNKS : docData.nbChunks;
   let chunkIdToUpdate = eventId + "-" + pickRandomChunckIndex(nbChunksForEvent);

   console.log(`userId= ${userId}, delta= ${delta}, absolute= ${absolute}, chunkId to update= ${chunkIdToUpdate}`);
   
   let historyChunkRef = db.collection(COLLECTION_HISTORY_CHUNKS).doc(chunkIdToUpdate);
   return historyChunkRef.get()
      .then(historyChunk => {

         // Get the history of deltas
         if (!historyChunk.exists) {
            console.log("ERROR - There is no historyChunk for this event. Skipping all.");
            return 0;
         }

         let inOuts = historyChunk.data().inOuts || [];

         // let now = new Date();

         let inOut = {
            delta: delta,
            absolute: absolute,
            timeStamp: now
         };

         if (userId !== undefined) {
            inOut.userId = userId;
         }

         inOuts.push(inOut);

         return historyChunkRef.set({
            timeStamp: now,
            inOuts: inOuts
         }, {merge: true});
      });
}

function pickRandomChunckIndex(max) {
   return Math.floor(Math.random() * max);
}

// Check that if user changed for subscription reason, we turn off all scheduled event.
exports.onUserUpdate = functions.firestore.document(COLLECTION_USER_EXTRA + '/{userId}').onUpdate((snap, context) => {
   
   let userId = context.params.userId;

   let upBefore = snap.before.data().plan || "";   
   let dnBefore = snap.before.data().displayName || "";
   let ptBefore = snap.before.data().pushToken || "";
   let emBefore = snap.before.data().email || "";
   
   console.log("user changed:" + userId);

   if (upBefore !== snap.after.data().plan) {
      console.log("user plan changed and is:" + snap.after.data().plan);
   }
   
   if (dnBefore !== snap.after.data().displayName) {
      console.log("user displayName changed, and is: " + snap.after.data().displayName);
   }
   
   if (ptBefore !== snap.after.data().pushToken) {
      console.log("user pushToken changed, and is: " + snap.after.data().pushToken);
   }
   
   if (emBefore !== snap.after.data().email) {
      console.log("user email changed, and is: " + snap.after.data().email);
   }
   
   
   
   /*if (userPlanBefore !== userPlanAfter && userPlanAfter === "free") {
      // Need to disable any scheduler for this user.
      return db.collection(COLLECTION_SCHEDULERS)
         .where("userId", "==", userId)
         .get()
         .then(schedulers => {
            if (schedulers.empty) {
               console.log("No scheduer for this user who is now in 'free' plan");
               return null;
            } else {
               // Need to set them all to disabled. Next time purchase, user will be able to reactivate them.
               // Also, in case there wae a 'rai' associated to them, they are reset. Next time the sheduler will execute, it won't create the RAI records.
               schedulers.forEach(scheduler => {
                  console.log("Will set to disabled all the shedulers of this user:" + scheduler.data().userId);
                  scheduler.ref.set({active: false, remoteAccessId:null}, {merge: true});
               });

               // We don't have to wait until this is finished.
               return null;
            }
         });
   } else {
      console.log(`User update (${userId}), not related to plan change, currently is ${userPlanAfter}`);
   }
    */
    
   return null;
});


exports.onDebugUpdated = functions.firestore.document('/debug/{debug}').onUpdate((snap, context) => {
   console.log("Debug data changed");
   let debugScheduler = (snap.before.data()['hourOfWeek'] !== snap.after.data()['hourOfWeek']);
   let debugDailyReport = (snap.before.data()['dailyReport'] !== snap.after.data()['dailyReport']);
   let debugWeeklyReport = (snap.before.data()['weeklyReport'] !== snap.after.data()['weeklyReport']);
   
   if (debugScheduler === true) {
      var hourOfWeek = snap.after.data()['hourOfWeek'];
      
      return checkShedulersForHourOfWeek(hourOfWeek)
         .then(() => {
            console.log("debug -- checkShedulersForHourOfWeek done");
            return buildAdminStats();
         })
         .then(() => {
            console.log("debug -- buildAdminStats done");
            return buildAdminSubscriptionsStats();
         });
   }
   else if (debugDailyReport === true) {
      return doTriggerReport(1, true);      
   }
   else if (debugWeeklyReport === true) {
      return doTriggerReport(7, true);      
   }
   else { 
      return 0;
   }
});


exports.onMessageUpdate = functions.firestore.document('/messages/{eventId}').onUpdate((snap, context) => {
   console.log(`New message for eventId = ${context.params.eventId}`);

   var messages = snap.after.data()['messages'];

   // This happens if a counter is reset.
   if (messages === undefined) {
      console.log("WARNING: Message record not found, must be a reset");
      return 0;
   }

   var lastMessage = messages[messages.length - 1];
   var lastMessagePostedBy = lastMessage.from;
   var toBeNotified;
   var senderDetails;
   
   // Now, look for the contributors of this counter.
   let docRef = db.collection(COLLECTION_EVENT_META_DATA).doc(context.params.eventId);

   return docRef
      .get()
      .then(doc => {
         if (!doc.exists) {
            console.log("WARNING - counter no longer exists, returning null");
            return null;
         } 
         else {
            var eventData = doc.data();
            var contributors = eventData['contributors'];
            toBeNotified = contributors.filter(item => item !== lastMessagePostedBy);

            console.log("contributors to be notified (sender being excluded)=" + JSON.stringify(toBeNotified));

            if (toBeNotified.length === 0) {
               // No need to go any further, there is no other contributor outside of the sender.
               console.log("No recipients outside of sender. Skipping");
               return null;
            }
            else {
               return toBeNotified;
            }
         }
      })
      .then(recipients => {
         if (recipients === null) {
            console.log("Returning null for previous reasons - 1");
            return null;
         }
         else {
            // We have the list of recipients, let's find the sender details
            return getDetailsForUserId(lastMessagePostedBy);
         }
      })
      .then(sender => {
         if (sender === null) {
            console.log("Returning null for previous reasons - 2");
            return null;
         }
         else {
            senderDetails = sender;
            
            let recipientsToFetchPromises = [];
            toBeNotified.forEach(r => {
               recipientsToFetchPromises.push(getDetailsForUserId(r));
            })
            // Now, get the details for each recipients.
            return Promise.all(recipientsToFetchPromises);
         }
      })
      .then(recipients => {
         if (recipients === null) {
            console.log("Returning null for previous reasons - 3");
            return null;
         } else {
            var payload = {
               data:{
                  action: 'newMessage',
                  content: lastMessage.message,
                  timeStamp: JSON.stringify(lastMessage.timeStamp),
                  fromId: lastMessage.from,
                  fromDisplayName: senderDetails['displayName']
               }
            };

            let photoURL = senderDetails['photoURL'];
            if (photoURL !== undefined && photoURL !== null) {
               payload.data.fromPhotoURL = photoURL;
            }

            let pushesToSend = [];
            recipients.forEach( recipient => {
               var pushToken = recipient['pushToken'];

               if ((pushToken !== undefined) && (pushToken !== null)) {
                  console.log("Should send push to pushToken= " + pushToken);
                  pushesToSend.push(admin.messaging().sendToDevice(pushToken, payload));
               }
               else {
                  console.log(`ERROR: user ${recipient['userId']} has no pushToken!!!`);
               }
            });
            return Promise.all(pushesToSend);
         }
      })
      .then(done => {
         console.log("Send push notif for new message done for all");
         return 0;
      });
   });


exports.validatePrivacyPolicy1 = functions.https.onCall((data, context) => {
   let userId = data.userId;

   console.log("in validatePrivacyPolicy1 for user:" + userId);

   let userRef = db.collection(COLLECTION_USER_EXTRA).doc(userId);

   return userRef
      .get()
      .then(userDoc => {
         if (!userDoc.exists) {
           return { success: false, reason: "user_not_found" };
        }

        // Now, update the user
        let now = new Date();
        let validatePolicyData = { privacyPolicy: true, privacyPolicyDate: now};
        return userRef.set(validatePolicyData, { merge: true})
           .then(updated => {
             return { success: true};
          });
      });
});

exports.sendRemoteAccessInstruction = functions.https.onCall((data, context) => { 
   
   let userId = data.userId;
   let email = data.email;
   let rai = data.rai || "EXAMPLE";
   let bc = data.bc || DEFAULT_BC;
   let tc = data.tc || DEFAULT_TC;
   let ts = data.ts || DEFAULT_TS;
   let la = data.la || DEFAULT_LA;

   let baseUrl = `https://${process.env.GCLOUD_PROJECT}.web.app/public?ownerId=${userId}&rai=${rai}&ts=${ts}&bc=${bc}&tc=${tc}&la=${la}`;
   let templateName = "support-remote-instructions-" + la;

   console.log(`sample URL=${baseUrl}`);

   let template = {
      name: templateName,
      data: {
         displayName: data.displayName,
         base: baseUrl
      }
   };

   let emailData = { 
      to: [ email ],
      bcc: [ "info@shared-counter.fr" ],
      template: template
   };

   return db.collection('mail')
      .add(emailData)
      .then(sent => {
         console.log('Queued email for delivery!');
         return { success: true };
      });
});

exports.joinEvent4 = functions.https.onCall((data, context) => {
   return doJoinEvent4(data, context);
});

function doJoinEvent4(data, context) {

   let eventRef = db.collection(COLLECTION_EVENT_META_DATA).doc(data.eventId);
   let callerId = data.userId;
   let eventId = data.eventId;
   let security;

   if (data.security !== undefined) {
      security = JSON.parse(data.security);
   }

   console.log("in joinEvent4, eventId=" + eventId + ", callerId=" + callerId + ", appVersionCode=" + data.appVersionCode + ", securityToken=" + security);

   return eventRef
      .get()
      .then(eventSnap => {
         if (!eventSnap.exists) {
            console.log('No such document! Event does not exist, so cant join it');
            return {
               success: false,
               reason: "event_not_found"
            };
         } else {
            // console.log('Document data:', doc.data());
            // Need to look for the event's
            let eventMetaData = eventSnap.data();

            // If the caller is already in the contributors, we just return the event as it is.
            if (eventMetaData.contributors.indexOf(callerId) >= 0) {
               console.log("caller was already in the list of contributors");
               return {
                   success: true,
                   doc: eventMetaData
                };
            }

            return checkSecurity()
            .then(securityPassed => {
               if (securityPassed === false) {
                  console.log("security checked failed. Returning error.");
                     return {
                        success: false,
                        reason: "same_account_used_multiple_times",
                        cure: "logout"
                  };
               }
               else {
                  // Now, from here, we need to check the owner plan, to see if this request can be fullfilled.
                  console.log("security checked success. Continue.");

                  return db.collection(COLLECTION_USER_EXTRA)
                     .doc(eventMetaData.owner)
                     .get()
                     .then(eventOwnerSnap => {
                        // Check what plan this user is in.
                        if (eventOwnerSnap.exists) {
                           // Ok, we have the owner
                           console.log("Owner found");
                           let ownerData = eventOwnerSnap.data();
                           let vip = ownerData.isVip || false;

                           if (vip) {
                              console.log("Owner is VIP joinEvent will succeed like for paid:" + callerId);
                           }

                           if (vip === true
                              || (ownerData.plan !== undefined && ownerData.plan.indexOf("subscription") >= 0 )) {

                              if (eventMetaData.contributors.length < MAX_CONTRIBUTORS_PAID) {

                                 updateSecurity(security, eventId);

                                 console.log("The user has a paid plan, counter has " + eventMetaData.contributors.length + " contributors. Adding a new one is allowed");
                                 return doUpdateContributors(eventRef, eventMetaData, callerId);
                              } else {
                                 console.log("The user has a paid plan, but has already reached " + MAX_CONTRIBUTORS_PAID + " contributors for this counter");
                                 return { success: false,
                                    reason: "number_of_contributors_quota_exceeded_paid",
                                    extra: String(MAX_CONTRIBUTORS_PAID) };
                                 }
                           } else if (eventMetaData.contributors.length < MAX_CONTRIBUTORS_FREE) {

                              updateSecurity(security, eventId);

                              console.log("The user has a free plan, but less than " + MAX_CONTRIBUTORS_FREE + " contributors for the moment. Let's update the counter");
                              return doUpdateContributors(eventRef, eventMetaData, callerId);
                           } else {

                              console.log("Owner's quota exceeded");
                              return { success: false,
                                 reason: "number_of_contributors_quota_exceeded_free",
                                 extra: String(MAX_CONTRIBUTORS_FREE) };
                           }
                        }
                  else {
                           console.log("Owner not found");
                           return { success: false, reason: "user_not_found"};
                        }
                     }
                 );
              }
          });
       }
    })
   .catch(err => {
         console.log('Error getting document', err);
         return {success: false, reason: "event_error" };
   });
}


/*
exports.joinEvent5 = functions.https.onCall((data, context) => {

   let eventRef = db.collection(COLLECTION_EVENT_META_DATA).doc(data.eventId);
   let callerId = data.userId;
   let eventId = data.eventId;
   let security;
   
   if (data.security !== undefined) {
      security = JSON.parse(data.security);
   } else {
      console.log("No security provided.");
   }
   
   return doJoinEvent5(eventRef, callerId, eventId, data.appVersionCode, security);
});
*/

exports.joinEvent6 = functions.https.onCall((data, context) => {
   // return doJoinEvent6(data, context);
   return doJoinEvent4(data, context);
});

// The core part of join event.
function doJoinEvent6(data, context) {
   let eventRef = db.collection(COLLECTION_EVENT_META_DATA).doc(data.eventId);
   let callerId = data.userId;
   let eventId = data.eventId;
   let security;
   let success = false;
   let reason;
   let eventMetaData = null;
   let eventOwner;
   let securityCheckResult;
   
   if (data.security !== undefined) {
      security = JSON.parse(data.security);
   }

   console.log("in joinEvent6, eventId=" + eventId + ", callerId=" + callerId + ", appVersionCode=" + data.appVersionCode + ", securityToken=" + security);

   return eventRef
      .get()
      .then(eventSnap => {
         if (!eventSnap.exists) {
            console.log('No such document! Event does not exist, so cant join it');
            reason = "event_not_found";
            return null;
         }
         else {
            // Need to look for the event's
            eventMetaData = eventSnap.data();
            return eventMetaData;
         }
      })
      .then(eventData => {
         if (eventData !== null) {
            // Now, get the owner
            return getDetailsForUserId(eventData.owner);
         }
         else {
            reason = reason || "user_not_found";
            console.log("failed because:" + reason);
            return null;
         }         
      })
      .then(owner => {
         if (owner !== null) {
            eventOwner = owner;
            let isVip = owner.isVip || false;
            return isVip || owner.plan.indexOf("subscription") >= 0;
         } else {
            console.log("failed because:" + reason);
            return null;
         }
      })
      .then(paid => {
         if (paid === true) {
            // Owner is paid, security always passes
            console.log("Paid owner, security always passes");
            return SECURITY_PASSES;
         }
         else
         if (paid === false) {
            return securityCheck2(eventId, eventMetaData, callerId, security);
         }
         else {
            return null;
         }
      })
      .then(securityCheck => {
         securityCheckResult = securityCheck;
         if (securityCheck === null) {
            console.log("Security check gave null. Reason=" + reason);
            return null;
         }
         else
         if ((securityCheck & SECURITY_PASSES) !== 0) {
            // Go for the remaining checks, such as number of contributors.
            let vip = eventOwner.isVip || false;

            if (vip) {
               console.log("Owner is VIP joinEvent will succeed like for paid:" + callerId);
            }

            if (vip === true
               || (eventOwner.plan.indexOf("subscription") >= 0 )) {

                  if (eventMetaData.contributors.length < MAX_CONTRIBUTORS_PAID || securityCheck === SECURITY_PASSED_SAME_USER_AND_DEVICE) {

                     // updateSecurity(security, eventId);
                     console.log("The user has a paid plan, counter has " + eventMetaData.contributors.length + " contributors. Adding a new one is allowed");
                     return doUpdateContributors(eventRef, eventMetaData, callerId);
                  }
                  else {
                     console.log("The user has a paid plan, but has already reached " + MAX_CONTRIBUTORS_PAID + " contributors for this counter");
                     reason = "number_of_contributors_quota_exceeded_paid";
                     return null;
                  }
               }
               else
               if (eventMetaData.contributors.length < MAX_CONTRIBUTORS_FREE || securityCheck === SECURITY_PASSED_SAME_USER_AND_DEVICE) {

                  // updateSecurity(security, eventId);

                  console.log("The user has a free plan, but less than " + MAX_CONTRIBUTORS_FREE + " contributors for the moment. Let's update the counter");
                  return doUpdateContributors(eventRef, eventMetaData, callerId);
               } else {

                  console.log("Owner's quota exceeded");
                  reason = "number_of_contributors_quota_exceeded_free";
                  return null;
               }
         }
         else {
            reason = "same_account_used_multiple_times";
            console.log("failed because:" + reason);
            return null;
         }
      })
      .then(contributorsUpdated => {
         contributorsUpdatedResult = contributorsUpdated;
         
         if (contributorsUpdated !== null) {
            if ((securityCheckResult & SECURITY_NEEDS_RECORD_UPDATE) !== 0) {
               return updateSecurity(security, eventId);
            } else {
               return true;
            }
         } else {
            return null;
         }
      })
      .then(end => {
         if (contributorsUpdatedResult !== null) {
            return contributorsUpdatedResult;
         } else {
            console.log("failed because:" + reason);
            return {
               success: false,
               reason: reason
            }
         }
      });
}

/*
function doJoinEvent5(eventRef, callerId, eventId, appVersionCode, security) {
   let addToSecurityIfNeeded = false;
   let eventData;
   let pipeline = {
      success: true
   };
   
   console.log("in joinEvent5, eventId=" + eventId + ", callerId=" + callerId + ", appVersionCode=" + appVersionCode + ", securityToken=" + security.token);

   return eventRef
      .get()
      .then(eventSnap => {
         if (!eventSnap.exists) {
            console.log('No such document! Event does not exist, so cant join it');
            pipeline.success = false;
            pipeline.reason = "event_not_found";
            return null;
         } else {
            console.log("event found ok");
            eventData = eventSnap.data();
            return eventData;
         }
      })
      .then(eventFoundStep => {
         if (eventFoundStep === null) { 
            return null;
         }
         
         // Ok, event found. Now check security
         return securityCheck2(eventId, eventData, callerId, security);       
      })
      .then(securityPassed => {

         if (securityPassed === null) {
            // Because event was not found.
            return null;
         }

         // So we can check later if we have to add or not a security record.
         pipeline.securityPassed = securityPassed;

         if ((securityPassed & SECURITY_PASSES) !== 0) {
            // Get the owner data.
            console.log("security - passed, now fetching owner details");
            return getEventOwner(eventData);
         } else { // SECURITY_FAILED_DUPLICATED_USER case
            // Event was not found, or security didn't pass.
            console.log("security - failed, returning same user error");
            pipeline.success = false;
            pipeline.reason = "same_account_used_multiple_times";
            return null;
         }
      })
      .then(owner => {
         if (owner === null) {
            return null;
         }
         else {
            // Security check passed. In case this use is already in the list of contributors, no need to have it updated.
            if (eventData.contributors.indexOf(callerId) >= 0) {

               // Because we're going to return null in this 'if', the security record update must be done now (if needed), 
               // otherwise, won't be done in next step. We rely on what the security step got returned before.
               if ((pipeline.securityPassed & SECURITY_NEEDS_RECORD_UPDATE) !== 0) {
                  console.log("security - Duplicated user and security says security update is required");
                  updateSecurity(security, eventId);
               } else {
                  console.log("security - Duplicated user but security says no security update required");
               }

               console.log("security control passed, and caller was already in the list of contributors. Nothing to do, it's a success.");
               pipeline = {
                   success: true,
                   doc: eventData
               };

               return null;
            }
            
            // We have the owner.
            let isVip = owner.isVip || false;
            let isPaid = owner.plan !== undefined && owner.plan.indexOf("subscription") >= 0;
            
            if (isPaid || isVip) {
               if (eventData.contributors.length < MAX_CONTRIBUTORS_PAID) {
                  console.log("The user has a paid plan or is VIP, counter has " + eventData.contributors.length + " contributors. Adding a new one is allowed");
                  return doUpdateContributors(eventRef, eventData, callerId);
               } else {
                  console.log("The user has a paid plan or is VIP, but has already reached " + MAX_CONTRIBUTORS_PAID + " contributors for this counter");
                  pipeline = {
                     success: false,
                     reason: "number_of_contributors_quota_exceeded_paid",
                     extra: String(MAX_CONTRIBUTORS_PAID)
                  };
                  // Breaks the pipeline.
                  return null;
               }
            }
            else
            if (eventData.contributors.length < MAX_CONTRIBUTORS_FREE) {
               console.log("The user has a free plan, counter has less than " + MAX_CONTRIBUTORS_FREE + " contributors for the moment. Adding a new one is allowed");
               return doUpdateContributors(eventRef, eventData, callerId);
            }
            else {
               console.log("Owner's free quota exceeded");
               pipeline = {
                  success: false,
                  reason: "number_of_contributors_quota_exceeded_free",
                  extra: String(MAX_CONTRIBUTORS_FREE)
               };

               return null;
            }
         }
      })
      .then(addedToContributors => {
         if (addedToContributors === null) {
            return null;
         }
         
         // Now, add the security record, if needed.
         if ((pipeline.securityPassed & SECURITY_NEEDS_RECORD_UPDATE) !== 0) {
            console.log("security - Last step reached, update security record required");
            updateSecurity(security, eventId);
         } else {
            console.log("security - Last step reached, but update security record not required");
         }
         return addedToContributors;
      })
      .then(last => {
         if (last === null) {
            return pipeline;
         }

         console.log("Creation completed");
         return last;
      })}
*/

// highest bit set means update required.
const SECURITY_NEEDS_RECORD_UPDATE             = 0b10000000;
const SECURITY_PASSES                          = 0b01000000;

const SECURITY_PASSED_NO_SECURITY_PROVIDED     = 0b00000001 | SECURITY_PASSES;
const SECURITY_PASSED_SAME_USER_AND_DEVICE     = 0b00000010 | SECURITY_PASSES;
const SECURITY_PASSED_NO_SECURITY_RECORD_FOUND = 0b00000011 | SECURITY_PASSES;
const SECURITY_PASSED_FIRST_TIME_SECURITY      = 0b00000100 | SECURITY_PASSES | SECURITY_NEEDS_RECORD_UPDATE;
const SECURITY_PASSED_OWNER_COURTESY           = 0b00000101 | SECURITY_PASSES | SECURITY_NEEDS_RECORD_UPDATE;
const SECURITY_FAILED_DUPLICATED_USER          = 0b00000110;

function securityCheck2(eventId, eventData, callerId, security) {
   if (security === undefined) {
      console.log("security - no security data provided. Assuming not suspicious (nice guy)")
      return Promise.resolve(SECURITY_PASSED_NO_SECURITY_PROVIDED);
   }
   
   let securityRef = db.collection(COLLECTION_SECURITY).doc(eventId);
   
   if (eventData.contributors.indexOf(callerId) < 0) {
      console.log("security - this user is not in the list of contributors. Nothing suspicious, returning passed = 'true'");
      return Promise.resolve(SECURITY_PASSED_FIRST_TIME_SECURITY);
   } else {
      console.log("security - this user is already in the list of contributors. Maybe legitimate, maybe not...");
      return securityRef
         .get()
         .then(securityData => {
            if (securityData.exists) {
               let multipleUsers = 0;
               let isOwner = (callerId === eventData.owner);
               
               console.log("Security check performed on onwer:" + isOwner);
               
               // We have security record for this event. Now analysing the content
               securityData.data().userSecurityChecks.forEach(securityEntry => { 

                  if ((securityEntry.userId === callerId) && (securityEntry.token !== security.token)) {
                     multipleUsers++;
                     console.log("Same account detected, but different devices");
                  }
               });
               
               // Now checking if in double.
               if (multipleUsers === 0) {
                  console.log("Not a duplicated user, all's good.");
                  return Promise.resolve(SECURITY_PASSED_SAME_USER_AND_DEVICE);
               } 
               else if (multipleUsers === 1 && isOwner) {
                  console.log("Is a duplicated user, but owner in courtesy duplications");
                  return Promise.resolve(SECURITY_PASSED_OWNER_COURTESY);
               }
               else {
                  console.log("Is a duplicated user. Nb multiples=" + multipleUsers);
                  return Promise.resolve(SECURITY_FAILED_DUPLICATED_USER);
               }
            }
            else {
               console.log("security - no security record found. Assuming not suspicious?");
               return Promise.resolve(SECURITY_PASSED_NO_SECURITY_RECORD_FOUND);
            }
         });
   }
}



function getEventOwner(eventData) { 
   return db.collection(COLLECTION_USER_EXTRA)
      .doc(eventData.owner)
      .get()
      .then(user => {
         if (user.exists) { 
            return user.data();
         } else { 
            return null;
         }
      });
}


function checkSecurity() {
   if (SECURITY_CHECK_SUPPORTED) {
      return Promise.resolve(true);
   } else {
      return Promise.resolve(true);
   }
}

function updateSecurity(security, eventId) {
   if (security !== undefined) {
      let securityRef = db.collection(COLLECTION_SECURITY).doc(eventId);

      return securityRef.get()
         .then(securitySnap => {
            if (securitySnap.exists) {
               let lst = securitySnap.data().userSecurityChecks || [];
               lst.push(security);

               console.log("Updated security check list=" +  JSON.stringify(lst));
               return lst;
            } else {
               console.log("Someone joined, but no security entry found, skipping - sounds like the counter was created by someone who does not have security data set");
               return null;
            }
         })
         .then(lst => {
            if (lst !== null) {
               return securityRef.set({userSecurityChecks: lst}, {merge: true});
            } else {
               return 0;
            }
         });
   }
   else {
      return 0;
   }
}

function doUpdateContributors(eventRef, eventMetaData, callerId){

   eventMetaData.contributors.push(callerId);

   return eventRef
      .set({contributors: eventMetaData.contributors}, {merge: true})
      .then(() => {
         console.log("Meta data updated with new contributor");
         return {
            success: true,
            doc: eventMetaData
         };
      }
   );
}


exports.getAllContributors1 = functions.https.onCall((data, context) => {
   let callerId = data.userId;
   console.log("caller " + callerId + " requested all contributors");
   let allUserExtra = [];

   // Get all contributors for which this user is a owner.
   return db.collection(COLLECTION_EVENT_META_DATA)
      .where("owner", "==", callerId)
      .get()
      .then(matches => {

         if (matches.empty) {
            console.log("No event/contributor found");

            // the caller is first, no matter what
            return [ callerId ];
         } else {
            console.log("Has found event(s)");

            // The caller is first, no matter what
            let allContributors = [ callerId ];

            matches.forEach(match => {
               // console.log("Will concatenate:" + match.data().contributors);
               allContributors = allContributors.concat(match.data().contributors);
            });
            console.log("All contributors:" + allContributors);

            let filtered = allContributors.filter(onlyUnique);
            console.log("All filtered contributors:" + filtered);

            return filtered;
         }
      })
      .then(list => {
         // For each, we want to get the email + name
         let getUserExtra = [];
         list.forEach(id => {
            console.log("Will get extra for " + id);
            getUserExtra.push(db.collection(COLLECTION_USER_EXTRA)
               .doc(id)
               .get()
               .then(x => {
                  if (x.exists) {
                     console.log("Found a user, getting details");
                     allUserExtra.push({
                        displayName: x.data().displayName,
                        email: x.data().email,
                        photoURL: x.data().photoURL,
                        userId: x.data().userId,
                        createdAt: x.data().createdAt
                     });
                  } else {
                     console.log("could not find user details");
                  }

                  return 0;
               }));
         });

         return Promise.all(getUserExtra);
      })
      .then(allFetched => {
         return allUserExtra.sort((a, b) => {
            try {
               if (a.userId === callerId) return -1;
               if (b.userId === callerId) return +1;
               return a.displayName < b.displayName ? -1 : +1;
            } catch(e) {
               return 0;
            }
         });
      })
   })


function onlyUnique(value, index, self) {
    return self.indexOf(value) === index;
}

