import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

// POST /auth/register — creates or updates user profile after Google Sign-In
router.post('/register', async (req: Request, res: Response) => {
  try {
    const { uid, displayName, email, timezone, defaultRisk } = req.body;
    if (!uid || !email) {
      res.status(400).json({ error: 'uid and email are required' });
      return;
    }
    const user = await prisma.user.upsert({
      where: { id: uid },
      update: { displayName, timezone, defaultRisk },
      create: { id: uid, email, displayName, timezone, defaultRisk }
    });
    res.status(201).json(user);
  } catch (err) {
    console.error('register error:', err);
    res.status(500).json({ error: 'Registration failed' });
  }
});

export default router;
