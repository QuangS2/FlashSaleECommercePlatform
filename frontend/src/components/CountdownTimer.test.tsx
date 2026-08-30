import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { CountdownTimer } from './CountdownTimer';

describe('CountdownTimer', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('render timer đúng định dạng và giảm đếm ngược', () => {
    render(<CountdownTimer targetHours={2} />);

    // Ban đầu là 02:45:30
    expect(screen.getByText('02')).toBeInTheDocument();
    expect(screen.getByText('45')).toBeInTheDocument();
    expect(screen.getByText('30')).toBeInTheDocument();

    act(() => {
      vi.advanceTimersByTime(2000);
    });

    // Giảm 2 giây -> 28
    expect(screen.getByText('28')).toBeInTheDocument();
  });
});
