const functions = require('firebase-functions');
const path = require('path');
const Moods = require(path.resolve( __dirname, "./moods.js" ));


// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();


exports.getFolloweeMoods = functions.https.onRequest(async (req, res) => {
  const uid = req.query.uid;

  const data = await db.doc(`users/${uid}`).get();
  const user = data.data();
  console.log(`Starting get Followee moods for user ${uid}`);

  const moods = [];
  if (user.following) {
    let promises = user.following.map(uid => Moods.iterateAllOfUser(db, uid, (mood) => moods.push(mood)));
    await Promise.all(promises);
  } else {
    console.log("No followees found");
  }

  res.send(moods);
});

exports.followUser = functions.https.onRequest(async (req, res) => {
  const uid = req.query.uid;
  const otherId = req.query.oid;

  const user = db.doc(`requests/${otherId}`);
  let userDataRef = await user.get();
  if (!userDataRef.exists) {
    await user.set({
      following: []
    });
    userDataRef = await user.get();
  }
  const userData = userDataRef.data();
  userData.following = [...(new Set((userData.following || []).concat([uid])))];
  user.set(userData);
  res.send("Success\n");
})


async function finalizeUserFollow(uid, otherId, isAccepted) {
  if (isAccepted) {
    const user = db.doc(`users/${otherId}`);
    const userData = (await user.get()).data();
    userData.following = [
      ...(new Set((userData.following || []).concat([uid])))
    ];
    user.set(userData);
  }

  await db.doc(`requests/${otherId}`).delete();

  return "Success\n";
}

exports.denyFollowUser = functions.https.onRequest(async (req, res) => {
  const uid = req.query.uid;
  const otherId = req.query.oid;

  res.send(await finalizeUserFollow(uid, otherId, false));
})

exports.confirmFollowUser = functions.https.onRequest(async (req, res) => {
  const uid = req.query.uid;
  const otherId = req.query.oid;

  res.send(await finalizeUserFollow(uid, otherId, true));
})

exports.unfollowUser = functions.https.onRequest(async (req, res) => {
  const uid = req.query.uid;
  const otherId = req.query.oid;

  const user = db.doc(`users/${uid}`);
  const userData = (await user.get()).data();

  const f = new Set(userData.following || []);
  if (f.has(otherId)) {
    f.delete(otherId);
  }

  userData.following = [...f];
  user.set(userData);

  res.send("Success\n");
})

exports.deleteAllMoods = functions.https.onRequest(async (req, res) => {
  const uid = req.query.uid;
  await Moods.iterateAllOfUser(db, uid, (mood) => {
    db.collection('users').doc(uid).collection('moods').doc(mood.id).delete();
  });
  res.send("Success\n");
})
