import React from 'react';
import { Adjustments } from './adjustments';

export function computeCSSFilter(adj: Adjustments): string {
  const filters: string[] = [];
  if (adj.brightness !== 0) filters.push(`brightness(${1 + adj.brightness / 5})`);
  if (adj.exposure !== 0) filters.push(`brightness(${1 + adj.exposure / 5})`);
  if (adj.contrast !== 0) filters.push(`contrast(${1 + adj.contrast / 100})`);
  if (adj.saturation !== 0) filters.push(`saturate(${1 + adj.saturation / 100})`);
  if (adj.temperature !== 0) {
    filters.push(`sepia(${Math.abs(adj.temperature) / 200})`);
    filters.push(`hue-rotate(${adj.temperature * 1.5}deg)`);
  }
  if (adj.tint !== 0) filters.push(`hue-rotate(${adj.tint * 0.8}deg)`);
  if (adj.vibrance !== 0) filters.push(`saturate(${1 + adj.vibrance / 150})`);
  if (adj.clarity !== 0) filters.push(`contrast(${1 + adj.clarity / 200})`);
  if (adj.dehaze !== 0) {
    filters.push(`contrast(${1 + adj.dehaze / 150})`);
    filters.push(`brightness(${1 + adj.dehaze / 300})`);
  }
  return filters.length > 0 ? filters.join(' ') : 'none';
}

export function computeTransform(adj: Adjustments): string {
  const parts: string[] = [];
  if (adj.rotation !== 0) parts.push(`rotate(${adj.rotation}deg)`);
  if (adj.flipH) parts.push('scaleX(-1)');
  if (adj.flipV) parts.push('scaleY(-1)');
  return parts.join(' ');
}

export function computeCropClipPath(cropX: number, cropY: number, cropW: number, cropH: number): string {
  return `inset(${cropY * 100}% ${(1 - cropX - cropW) * 100}% ${(1 - cropY - cropH) * 100}% ${cropX * 100}%)`;
}

export function getVignetteStyle(amount: number, feather: number, midpoint: number): React.CSSProperties {
  if (amount === 0) return {};
  const opacity = Math.abs(amount) / 100 * 0.6;
  return {
    background: `radial-gradient(ellipse at center, transparent ${midpoint}%, rgba(0,0,0,${opacity}) 100%)`,
    mixBlendMode: 'multiply',
  };
}

export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
