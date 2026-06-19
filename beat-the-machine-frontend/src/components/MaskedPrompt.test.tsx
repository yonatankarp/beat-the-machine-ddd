import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import MaskedPrompt from './MaskedPrompt'

describe('MaskedPrompt', () => {
  it('renders revealed words and blanks sized by length', () => {
    render(
      <MaskedPrompt
        tokens={[
          { revealed: true, word: 'fear', length: 4 },
          { revealed: false, word: null, length: 2 },
          { revealed: false, word: null, length: 3 },
        ]}
      />,
    )
    expect(screen.getByText('fear')).toBeInTheDocument()
    // Two hidden words: one with 2 blanks, one with 3 blanks => 5 blank cells total.
    expect(screen.getAllByTestId('blank')).toHaveLength(5)
  })

  it('exposes the prompt as a labelled group', () => {
    render(<MaskedPrompt tokens={[{ revealed: false, word: null, length: 3 }]} />)
    expect(screen.getByRole('group', { name: 'Prompt to guess' })).toBeInTheDocument()
  })

  it('labels each hidden word with its length for screen readers', () => {
    render(
      <MaskedPrompt
        tokens={[
          { revealed: false, word: null, length: 4 },
          { revealed: false, word: null, length: 2 },
        ]}
      />,
    )
    expect(screen.getByLabelText('4-letter word, hidden')).toBeInTheDocument()
    expect(screen.getByLabelText('2-letter word, hidden')).toBeInTheDocument()
  })
})
