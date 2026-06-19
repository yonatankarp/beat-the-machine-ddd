import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { PictureStatus } from '../generated'
import ChallengePicture from './ChallengePicture'

describe('ChallengePicture', () => {
  it('renders the image when READY', () => {
    render(<ChallengePicture picture={{ status: PictureStatus.Ready, url: 'https://x/y.png' }} />)
    expect(screen.getByRole('img')).toBeInTheDocument()
  })

  it('shows "Image unavailable" when FAILED and no img', () => {
    render(<ChallengePicture picture={{ status: PictureStatus.Failed, url: null }} />)
    expect(screen.getByText('Image unavailable')).toBeInTheDocument()
    expect(screen.queryByRole('img')).toBeNull()
  })

  it('shows "Generating image…" when PENDING and no img', () => {
    render(<ChallengePicture picture={{ status: PictureStatus.Pending, url: null }} />)
    expect(screen.getByText('Generating image…')).toBeInTheDocument()
    expect(screen.queryByRole('img')).toBeNull()
  })
})
