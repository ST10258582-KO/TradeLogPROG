import { Router, Response } from 'express';
import { PrismaClient } from '@prisma/client';
import { AuthRequest } from '../middleware/auth';

const router = Router();
const prisma = new PrismaClient();

router.get('/', async (req: AuthRequest, res: Response) => {
  try {
    const tags = await prisma.tag.findMany({ where: { userId: req.uid! } });
    res.json({ tags });
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch tags' });
  }
});

router.post('/', async (req: AuthRequest, res: Response) => {
  try {
    const tag = await prisma.tag.create({ data: { userId: req.uid!, name: req.body.name } });
    res.status(201).json(tag);
  } catch (err) {
    res.status(500).json({ error: 'Failed to create tag' });
  }
});

router.delete('/:id', async (req: AuthRequest, res: Response) => {
  try {
    await prisma.tag.deleteMany({ where: { id: req.params.id, userId: req.uid! } });
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: 'Failed to delete tag' });
  }
});

export default router;
