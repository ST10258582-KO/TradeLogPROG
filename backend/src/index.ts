import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import * as admin from 'firebase-admin';
import dotenv from 'dotenv';
import { requireAuth } from './middleware/auth';
import authRouter from './routes/auth';
import tradesRouter from './routes/trades';
import analyticsRouter from './routes/analytics';
import tagsRouter from './routes/tags';
import settingsRouter from './routes/settings';

dotenv.config();

// Initialise Firebase Admin — env var (Railway/cloud) takes priority, falls back to local JSON file
let firebaseCredential: admin.credential.Credential;
if (process.env.FIREBASE_SERVICE_ACCOUNT) {
  const sa = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT) as admin.ServiceAccount;
  firebaseCredential = admin.credential.cert(sa);
} else {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const sa = require('../firebase-service-account.json') as admin.ServiceAccount;
  firebaseCredential = admin.credential.cert(sa);
}
admin.initializeApp({ credential: firebaseCredential });

const app = express();
const PORT = process.env.PORT ?? 8080;

app.use(helmet());
app.use(cors());
app.use(express.json());

// Health check — used by Azure App Service
app.get('/health', (_req, res) => res.json({ status: 'ok', timestamp: new Date().toISOString() }));

// Routes
app.use('/auth', authRouter);
app.use('/trades', requireAuth, tradesRouter);
app.use('/analytics', requireAuth, analyticsRouter);
app.use('/tags', requireAuth, tagsRouter);
app.use('/settings', requireAuth, settingsRouter);

app.listen(PORT, () => console.log(`TradeLog API running on port ${PORT}`));
