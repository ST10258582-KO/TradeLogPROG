import { Router, Response } from 'express';
import { PrismaClient } from '@prisma/client';
import { AuthRequest } from '../middleware/auth';

const router = Router();
const prisma = new PrismaClient();

function classifySession(entryTime: Date): string {
  const hour = entryTime.getUTCHours();
  if (hour >= 7 && hour < 16) return 'LONDON';
  if (hour >= 12 && hour < 21) return 'NEW_YORK';
  if (hour >= 23 || hour < 8) return 'ASIA';
  return 'OTHER';
}

function calcRMultiple(direction: string, entry: number, sl: number, exit: number | null): number | null {
  if (exit === null) return null;
  const risk = Math.abs(entry - sl);
  if (risk === 0) return null;
  return direction === 'BUY'
    ? (exit - entry) / risk
    : (entry - exit) / risk;
}

function calcPnl(direction: string, entry: number, exit: number | null, qty: number): number | null {
  if (exit === null) return null;
  return direction === 'BUY'
    ? (exit - entry) * qty
    : (entry - exit) * qty;
}

// GET /trades
router.get('/', async (req: AuthRequest, res: Response) => {
  try {
    const { tag, session } = req.query;
    const trades = await prisma.trade.findMany({
      where: {
        userId: req.uid!,
        ...(session ? { session: String(session) } : {}),
        ...(tag ? { tags: { contains: String(tag) } } : {})
      },
      orderBy: { entryTime: 'desc' }
    });
    res.json({ trades, total: trades.length });
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch trades' });
  }
});

// POST /trades
router.post('/', async (req: AuthRequest, res: Response) => {
  try {
    const { instrument, direction, entryPrice, stopLoss, takeProfit, exitPrice, quantity, notes, tags, entryTime, exitTime } = req.body;
    const entry = new Date(entryTime);
    const exit = exitPrice ?? null;
    const trade = await prisma.trade.create({
      data: {
        userId: req.uid!,
        instrument,
        direction,
        entryPrice,
        stopLoss,
        takeProfit,
        exitPrice: exit,
        quantity,
        rMultiple: calcRMultiple(direction, entryPrice, stopLoss, exit),
        pnl: calcPnl(direction, entryPrice, exit, quantity),
        session: classifySession(entry),
        status: exit !== null ? 'CLOSED' : 'OPEN',
        notes: notes ?? null,
        tags: Array.isArray(tags) ? tags.join(',') : (tags ?? ''),
        entryTime: entry,
        exitTime: exitTime ? new Date(exitTime) : null,
        syncedAt: new Date()
      }
    });
    res.status(201).json(trade);
  } catch (err) {
    console.error('createTrade error:', err);
    res.status(500).json({ error: 'Failed to create trade' });
  }
});

// GET /trades/:id
router.get('/:id', async (req: AuthRequest, res: Response) => {
  try {
    const trade = await prisma.trade.findFirst({ where: { id: req.params.id, userId: req.uid! } });
    if (!trade) { res.status(404).json({ error: 'Trade not found' }); return; }
    res.json(trade);
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch trade' });
  }
});

// PUT /trades/:id
router.put('/:id', async (req: AuthRequest, res: Response) => {
  try {
    const existing = await prisma.trade.findFirst({ where: { id: req.params.id, userId: req.uid! } });
    if (!existing) { res.status(404).json({ error: 'Trade not found' }); return; }
    const { instrument, direction, entryPrice, stopLoss, takeProfit, exitPrice, quantity, notes, tags, entryTime, exitTime } = req.body;
    const exit = exitPrice ?? null;
    const trade = await prisma.trade.update({
      where: { id: req.params.id },
      data: {
        instrument, direction, entryPrice, stopLoss, takeProfit,
        exitPrice: exit, quantity, notes, tags,
        rMultiple: calcRMultiple(direction, entryPrice, stopLoss, exit),
        pnl: calcPnl(direction, entryPrice, exit, quantity),
        status: exit !== null ? 'CLOSED' : 'OPEN',
        entryTime: new Date(entryTime),
        exitTime: exitTime ? new Date(exitTime) : null,
        syncedAt: new Date()
      }
    });
    res.json(trade);
  } catch (err) {
    res.status(500).json({ error: 'Failed to update trade' });
  }
});

// DELETE /trades/:id
router.delete('/:id', async (req: AuthRequest, res: Response) => {
  try {
    const existing = await prisma.trade.findFirst({ where: { id: req.params.id, userId: req.uid! } });
    if (!existing) { res.status(404).json({ error: 'Trade not found' }); return; }
    await prisma.trade.delete({ where: { id: req.params.id } });
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: 'Failed to delete trade' });
  }
});

export default router;
