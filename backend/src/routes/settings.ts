import { Router, Response } from 'express';
import { PrismaClient } from '@prisma/client';
import { AuthRequest } from '../middleware/auth';

const router = Router();
const prisma = new PrismaClient();

router.get('/', async (req: AuthRequest, res: Response) => {
  try {
    const user = await prisma.user.findUnique({ where: { id: req.uid! } });
    if (!user) { res.status(404).json({ error: 'User not found' }); return; }
    res.json({
      displayName: user.displayName, timezone: user.timezone,
      defaultRisk: user.defaultRisk, currency: user.currency,
      language: user.language, notifications: user.notifications
    });
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch settings' });
  }
});

router.put('/', async (req: AuthRequest, res: Response) => {
  try {
    const { displayName, timezone, defaultRisk, currency, language, notifications } = req.body;
    const user = await prisma.user.update({
      where: { id: req.uid! },
      data: { displayName, timezone, defaultRisk, currency, language, notifications }
    });
    res.json({
      displayName: user.displayName, timezone: user.timezone,
      defaultRisk: user.defaultRisk, currency: user.currency,
      language: user.language, notifications: user.notifications
    });
  } catch (err) {
    res.status(500).json({ error: 'Failed to update settings' });
  }
});

export default router;
