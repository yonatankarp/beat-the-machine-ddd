import { useQuery } from '@tanstack/react-query'
import { getChallenge } from '../api/challenges'

export function useChallenge(id: string) {
  return useQuery({
    queryKey: ['challenge', id],
    queryFn: () => getChallenge(id),
    refetchInterval: (query) =>
      query.state.data?.picture.status === 'PENDING' ? 1500 : false,
  })
}
