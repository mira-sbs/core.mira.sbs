package sbs.mira.core.model.match;

/**
 * This enumerated type ensures accuracy & readability when
 * defining the the status of a round.
 * <p>
 * The NONE status is used when no match has been established.
 */
public
enum MiraMatchState
{
  START,
  VOTE,
  PRE_GAME,
  GAME,
  POST_GAME,
  END,
  FAILED
}