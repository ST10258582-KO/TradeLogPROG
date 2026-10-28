import { Router, Response } from 'express';
import { PrismaClient } from '@prisma/client';
import { AuthRequest } from '../middleware/auth';

const router = Router();
const prisma = new PrismaClient();

// GET /analytics/summary
router.get('/summary', async (req: AuthRequest, res: Response) => {
  try {
    const trades = await prisma.trade.findMany({ where: { userId: req.uid!, status: 'CLOSED' } });
    const total = trades.length;
    const wins = trades.filter(t => (t.pnl ?? 0) > 0).length;
    const netPnl = trades.reduce((sum, t) => sum + (t.pnl ?? 0), 0);
    const avgR = total > 0 ? trades.reduce((sum, t) => sum + (t.rMultiple ?? 0), 0) / total : 0;
    res.json({ winRate: total > 0 ? wins / total : 0, netPnl, avgR, tradeCount: total });
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch summary' });
  }
});

// GET /analytics/equity-curve
router.get('/equity-curve', async (req: AuthRequest, res: Response) => {
  try {
    const trades = await prisma.trade.findMany({
      where: { userId: req.uid!, status: 'CLOSED' },
      orderBy: { exitTime: 'asc' }
    });
    let cumulative = 0;
    const points = trades.map(t => {
      cumulative += t.pnl ?? 0;
      return { date: (t.exitTime ?? t.entryTime).toISOString().split('T')[0], cumulativePnl: cumulative };
    });
    res.json({ points });
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch equity curve' });
  }
});

// GET /analytics/by-session
router.get('/by-session', async (req: AuthRequest, res: Response) => {
  try {
    const trades = await prisma.trade.findMany({ where: { userId: req.uid!, status: 'CLOSED' } });
    const sessions = ['LONDON', 'NEW_YORK', 'ASIA'];
    const stats: Record<string, { winRate: number; avgR: number; netPnl: number; tradeCount: number }> = {};
    for (const s of sessions) {
      const filtered = trades.filter(t => t.session === s);
      const wins = filtered.filter(t => (t.pnl ?? 0) > 0).length;
      stats[s] = {
        winRate: filtered.length > 0 ? wins / filtered.length : 0,
        avgR: filtered.length > 0 ? filtered.reduce((a, t) => a + (t.rMultiple ?? 0), 0) / filtered.length : 0,
        netPnl: filtered.reduce((a, t) => a + (t.pnl ?? 0), 0),
        tradeCount: filtered.length
      };
    }
    res.json({ london: stats.LONDON, newYork: stats.NEW_YORK, asia: stats.ASIA });
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch session stats' });
  }
});

// GET /analytics/by-tag
router.get('/by-tag', async (req: AuthRequest, res: Response) => {
  try {
    const trades = await prisma.trade.findMany({ where: { userId: req.uid!, status: 'CLOSED' } });
    const tagMap = new Map<string, { wins: number; total: number; rSum: number; pnlSum: number }>();
    for (const t of trades) {
      for (const tag of t.tags) {
        const entry = tagMap.get(tag) ?? { wins: 0, total: 0, rSum: 0, pnlSum: 0 };
        entry.total++;
        if ((t.pnl ?? 0) > 0) entry.wins++;
        entry.rSum += t.rMultiple ?? 0;
        entry.pnlSum += t.pnl ?? 0;
        tagMap.set(tag, entry);
      }
    }
    const tags = Array.from(tagMap.entries()).map(([name, v]) => ({
      name,
      winRate: v.total > 0 ? v.wins / v.total : 0,
      avgR: v.total > 0 ? v.rSum / v.total : 0,
      netPnl: v.pnlSum
    }));
    res.json({ tags });
  } catch (err) {
    res.status(500).json({ error: 'Failed to fetch tag stats' });
  }
});

export default router;
