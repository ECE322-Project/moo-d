const functions = require('firebase-functions');
const admin = require('firebase-admin');

async function iterateAllOfUser(db, userId, callbackEachMood) {
  console.log(`Getting moods for 'users/${userId}/moods'`)
  const data = await db.collection(`users/${userId}/moods`).get();
  const owner = (await db.doc(`users/${userId}`).get()).data();
  data.forEach(d => {
    console.log(`Got a mood for ${userId}: ${d.id}`);
    let data = d.data();
    data.owner = {
      id: userId,
      username: owner.username
    }
    data.id = d.id;
    callbackEachMood(data);
  })
}


module.exports = {
  iterateAllOfUser
}
