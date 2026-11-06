import { Request, Response, NextFunction } from 'express';
import * as admin from 'firebase-admin';
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

export interface AuthRequest extends Request {
  uid?: string;
}

export function requireAuth(req: AuthRequest, res: Response, next: NextFunction): void {
  const authHeader = req.headers.authorization;
  if (!authHeader?.startsWith('Bearer ')) {
    res.status(401).json({ error: 'Missing authorization token' });
    return;
  }
  const token = authHeader.split('Bearer ')[1];
  admin.auth().verifyIdToken(token)
    .then(async decoded => {
      req.uid = decoded.uid;
      // Auto-create the user row if it doesn't exist yet (handles first-time sign-in)
      await prisma.user.upsert({
        where: { id: decoded.uid },
        update: {},
        create: {
          id: decoded.uid,
          email: decoded.email ?? `${decoded.uid}@unknown.com`,
          displayName: decoded.name ?? 'Trader'
        }
      });
      next();
    })
    .catch(() => {
      res.status(401).json({ error: 'Invalid or expired token' });
    });
}
