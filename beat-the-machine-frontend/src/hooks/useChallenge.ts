import { useEffect, useRef } from 'react'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { getChallenge } from '../api/challenges'
import type { ApiError, ChallengeResponse } from '../api/challenges'
import { ChallengeStatus, PictureStatus } from '../generated'

const POLL_INTERVAL_MS = 1500
const MAX_POLLS = 40

export function useChallenge(id: string) {
  const polls = useRef(0)

  useEffect(() => {
    polls.current = 0
  }, [id])

  return useQuery<ChallengeResponse, ApiError>({
    queryKey: ['challenge', id],
    queryFn: () => getChallenge(id),
    placeholderData: keepPreviousData,
    refetchInterval: (query) => {
      const data = query.state.data
      const shouldPoll =
        data?.status === ChallengeStatus.InProgress &&
        data.picture.status === PictureStatus.Pending
      if (!shouldPoll) return false
      if (polls.current >= MAX_POLLS) return false
      polls.current += 1
      return POLL_INTERVAL_MS
    },
  })
}
