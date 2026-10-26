const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated read on portfolio fails", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("portfolio").get());
});

test("Authenticated user can read portfolio", async () => {
  const clientDb = testEnv.authenticatedContext("user123").firestore();
  await assertSucceeds(clientDb.collection("portfolio").get());
});

test("Authenticated user can create valid portfolio item", async () => {
  const clientDb = testEnv.authenticatedContext("user123").firestore();
  await assertSucceeds(
    clientDb.collection("portfolio").doc("item1").set({
      title: "SS Brand Identity",
      category: "Branding",
      description: "Full brand guidelines and visual asset suite",
      featured: true
    })
  );
});

test("Invalid project creation fails validation", async () => {
  const clientDb = testEnv.authenticatedContext("user123").firestore();
  // Missing required progressPercent
  await assertFails(
    clientDb.collection("projects").doc("proj1").set({
      clientId: "user123",
      title: "Website Redesign",
      status: "In Progress"
    })
  );
});

test("Authenticated client can create valid project", async () => {
  const clientDb = testEnv.authenticatedContext("user123").firestore();
  await assertSucceeds(
    clientDb.collection("projects").doc("proj1").set({
      clientId: "user123",
      title: "Website Redesign",
      status: "In Progress",
      progressPercent: 45
    })
  );
});

test("Client can access their own notifications and cannot access others", async () => {
  const clientDb = testEnv.authenticatedContext("user123").firestore();
  const otherDb = testEnv.authenticatedContext("otherUser").firestore();

  await assertSucceeds(
    clientDb.collection("users").doc("user123").collection("notifications").doc("notif1").set({
      userId: "user123",
      title: "Milestone Reached",
      message: "Logo concepts are ready for review",
      type: "project_update",
      read: false
    })
  );

  // Other user cannot read user123's notifications
  await assertFails(
    otherDb.collection("users").doc("user123").collection("notifications").doc("notif1").get()
  );
});

test("User can read and write to their chat messages", async () => {
  const clientDb = testEnv.authenticatedContext("user123").firestore();
  await assertSucceeds(
    clientDb.collection("users").doc("user123").collection("messages").doc("msg1").set({
      userId: "user123",
      sender: "user",
      senderName: "Client Aayush",
      text: "Namaste Nidhi, what is the status of our brand deliverables?"
    })
  );

  const otherDb = testEnv.authenticatedContext("otherUser").firestore();
  await assertFails(
    otherDb.collection("users").doc("user123").collection("messages").doc("msg1").get()
  );
});

test("Authenticated user can create and read valid GST Invoice", async () => {
  const clientDb = testEnv.authenticatedContext("user123").firestore();
  await assertSucceeds(
    clientDb.collection("invoices").doc("inv1").set({
      invoiceNumber: "SSC-2026-001",
      clientId: "user123",
      clientName: "Royal Crafts Ltd",
      clientGstin: "08AABCR1234F1Z8",
      subtotal: 50000,
      cgstAmount: 4500,
      sgstAmount: 4500,
      igstAmount: 0,
      totalAmount: 59000,
      status: "Pending",
      isInterState: false
    })
  );

  const doc = await clientDb.collection("invoices").doc("inv1").get();
  assertSucceeds(Promise.resolve(doc));
});

test("Invalid GST Invoice creation fails validation", async () => {
  const clientDb = testEnv.authenticatedContext("user123").firestore();
  // Missing required totalAmount and status
  await assertFails(
    clientDb.collection("invoices").doc("inv_bad").set({
      invoiceNumber: "SSC-2026-BAD",
      clientName: "Incomplete Client"
    })
  );
});
